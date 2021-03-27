package net.titanrealms.slime.codec;

import com.github.luben.zstd.Zstd;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.palette.PaletteStorage;
import net.minestom.server.instance.palette.Section;
import net.minestom.server.world.biomes.Biome;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
----------
“Slime” file format
2 bytes - magic = 0xB10B
1 byte (ubyte) - version, current = 0x03
2 bytes (short) - xPos of chunk lowest x & lowest z
2 bytes (short) - zPos
2 bytes (ushort) - width
2 bytes (ushort) - depth
[depends] - chunk bitmask
  -> each chunk is 1 bit: 0 if all air (missing), 1 if present
  -> chunks are ordered zx, meaning
  -> the last byte has unused bits on the right
  -> size is ceil((width*depth) / 8) bytes

4 bytes (int) - compressed chunks size
4 bytes (int) - uncompressed chunks size
  <array of chunks> (size determined from bitmask)
  compressed using zstd

4 bytes (int) - compressed tile entities size
4 bytes (int) - uncompressed tile entities size
  <array of tile entity nbt compounds>
  same format as mc,
  inside an nbt list named “tiles”, in global compound, no gzip anywhere
  compressed using zstd

1 byte (boolean) - has entities
[if has entities]
  4 bytes (int) compressed entities size
  4 bytes (int) uncompressed entities size
    <array of entity nbt compounds>
    Same format as mc EXCEPT optional “CustomId”
    in side an nbt list named “entities”, in global compound
    Compressed using zstd

4 bytes (int) - compressed “extra” size
4 bytes (int) - uncompressed “extra” size
[depends] - compound tag compressed using zstd

Custom chunk format
256 ints - heightmap
256 bytes - biomes
2 bytes - sections bitmask (bottom to top)
  2048 bytes - block light
  4096 bytes - blocks
  2048 bytes - data
  2048 bytes - skylight
  2 bytes (ushort) - HypixelBlocks3 size (0 if absent)
  [depends] - HypixelBlocks3
  For each section
 */
public interface SlimeCodec {
    short MAGIC = (short) 0xb10b;

    default byte[] encodeInstance(Instance instance) throws IOException {
        Collection<Chunk> chunks = instance.getChunks();

        BiFunction<ToIntFunction<Chunk>, Function<IntStream, OptionalInt>, Short> edge = (axe, math) -> (short) math.apply(chunks.stream().mapToInt(axe)).orElse(0);

        short minX = edge.apply(Chunk::getChunkX, IntStream::min); // 2 bytes (short) - xPos of chunk lowest x & lowest z
        short minZ = edge.apply(Chunk::getChunkZ, IntStream::min); // 2 bytes (short) - zPos
        short maxX = edge.apply(Chunk::getChunkX, IntStream::max);
        short maxZ = edge.apply(Chunk::getChunkZ, IntStream::max);

        short width = (short) (maxX - minX + 1); // 2 bytes (ushort) - width
        short depth = (short) (maxZ - minZ + 1); // 2 bytes (ushort) - depth

        byte[] chunkBitmask = this.createChunkBitmask(chunks, width, depth, minZ, minX); // chunk bitmask
        int padding = (int) Math.ceil((width * depth) / 8f) - chunkBitmask.length;
        byte[] bitmaskPadding = new byte[padding];
        Arrays.fill(bitmaskPadding, (byte) 0);

        byte[] uncompressedChunks = this.encodeChunks(chunks); // <array of chunks> (size determined from bitmask)

        byte[] compressedChunks = Zstd.compress(uncompressedChunks); // compressed using zstd

        int compressedChunkSize = compressedChunks.length; // 4 bytes (int) - compressed chunks size
        int uncompressedChunkSize = uncompressedChunks.length; // 4 bytes (int) - uncompressed chunks size

        CodecStream stream = CodecStream.create();
        stream.out().writeShort(MAGIC);

        stream.out().writeShort(minX);
        stream.out().writeShort(minZ);

        stream.out().writeShort(width);
        stream.out().writeShort(depth);

        stream.out().write(chunkBitmask);
        stream.out().write(bitmaskPadding);

        stream.out().writeInt(compressedChunkSize);
        stream.out().writeInt(uncompressedChunkSize);
        stream.out().write(compressedChunks);
        return stream.byteOut().toByteArray();
    }

    default byte[] encodeChunks(Collection<Chunk> chunks) throws IOException {
        CodecStream stream = CodecStream.create();
        for (Chunk chunk : chunks) {
            stream.out().write(this.encodeChunk(chunk));
        }
        return stream.byteOut().toByteArray();
    }

    default byte[] encodeChunk(Chunk chunk) throws IOException {
        CodecStream stream = CodecStream.create();

        Section[] sections = chunk.getFreshFullDataPacket().paletteStorage.getSections();

        int[] heightMap = new int[256]; // ignore for now - hardcoded in minestom

        for (int biome : Arrays.stream(chunk.getBiomes()).map(Biome::getId).collect(Collectors.toList())) {
            stream.out().writeInt(biome);
        }

        byte[] sectionsBitmask = this.createSectionsBitmask(chunk);
        stream.out().write(sectionsBitmask);
        for (int i = 0; i < 2 - sectionsBitmask.length; i++) { // padding
            stream.out().write(0);
        }

        for (Section section : sections) {
            byte[] blockLight = new byte[2048]; // ignore for now - hardcoded in minestom
            long[] blocks = section.getBlocks();
            for (long block : blocks) {
                stream.out().writeLong(block);
            }
            byte[] data = new byte[2048];
            byte[] skylight = new byte[2048]; // ignore for now - hardcoded in minestom
        }
        return stream.byteOut().toByteArray();
    }

    default byte[] createChunkBitmask(Collection<Chunk> chunks, short width, short depth, short minZ, short minX) {
        BitSet chunkBitSet = new BitSet(width * depth);
        for (Chunk chunk : chunks) {
            boolean isEmpty = true;
            int index = (chunk.getChunkZ() - minZ) * width + (chunk.getChunkX() - minX);
            PaletteStorage paletteStorage = chunk.getFreshFullDataPacket().paletteStorage;
            for (Section section : paletteStorage.getSections()) {
                long[] blocks = section.getBlocks();
                if (blocks == null || blocks.length == 0) {
                    isEmpty = false;
                    break;
                }
                isEmpty = Arrays.stream(blocks).anyMatch((block) -> block == 0);
            }
            chunkBitSet.set(index, isEmpty ? 0 : 1);
        }
        return chunkBitSet.toByteArray();
    }

    default byte[] createSectionsBitmask(Chunk chunk) {
        Section[] sections = chunk.getFreshFullDataPacket().paletteStorage.getSections();
        BitSet sectionsBitSet = new BitSet(16);
        for (int i = 0; i < sections.length; i++) {
            sectionsBitSet.set(i, sections[i] != null);
        }
        return sectionsBitSet.toByteArray();
    }
}
