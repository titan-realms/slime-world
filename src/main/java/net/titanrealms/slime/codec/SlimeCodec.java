package net.titanrealms.slime.codec;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

import java.util.BitSet;
import java.util.Collection;

public interface SlimeCodec {
    short MAGIC = (short) 0xb10b;

    default Instance decodeSlime(byte[] slime) {
        return null;
    }

    default byte[] encodeInstance(Instance instance) {
        Collection<Chunk> chunks = instance.getChunks();
        short minX = (short) chunks.stream().mapToInt(Chunk::getChunkX).min().orElse(0); // 2 bytes (short) - xPos of chunk lowest x & lowest z
        short minZ = (short) chunks.stream().mapToInt(Chunk::getChunkZ).max().orElse(0); // 2 bytes (short) - zPos
        short maxX = (short) chunks.stream().mapToInt(Chunk::getChunkX).min().orElse(0);
        short maxZ = (short) chunks.stream().mapToInt(Chunk::getChunkZ).max().orElse(0);

        short width = (short) (maxX - minX + 1); // 2 bytes (ushort) - width
        short depth = (short) (maxZ - minZ + 1); // 2 bytes (ushort) - depth

        BitSet chunkBitSet = new BitSet(width * depth);
        for (Chunk chunk : chunks) {
            int index = 0;
        }
        return new byte[0];
    }
}
