package net.titanrealms.titan;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.world.biomes.Biome;
import net.titanrealms.titan.objects.TitanWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TitanChunkLoader implements IChunkLoader {
    private final TitanWorld titanWorld;

    private static final Biome TITAN_BIOME = Biome.builder()
            .category(Biome.Category.NONE)
            .name(NamespaceID.from("minecraft:titan"))
            .temperature(1F)
            .downfall(0)
            .depth(0.125F)
            .scale(0.05F)
            .build();

    public TitanChunkLoader(TitanWorld titanWorld) {
        this.titanWorld = titanWorld;
    }

    public TitanChunkLoader() {
        this(new TitanWorld());
    }

    @Override
    public boolean loadChunk(@NotNull Instance instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        byte[] serialized = this.titanWorld.getChunk(chunkX, chunkZ);
        try {
            Chunk chunk = this.deserializeChunk(chunkX, chunkZ, serialized);
            if (callback != null) {
                callback.accept(chunk);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk, @Nullable Runnable callback) {
        Runnable tryExecuteCallback = () -> {
            if (callback != null) {
                callback.run();
            }
        };
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        try {
            this.titanWorld.setChunk(chunkX, chunkZ, this.serializeChunk(chunk));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        tryExecuteCallback.run();
    }

    private byte[] serializeChunk(Chunk chunk) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);

        ChunkDataPacket chunkData = chunk.getFreshFullDataPacket();
        for (byte x = 0; x < 16; x++) {
            for (short y = 0; y < 256; y++) {
                for (byte z = 0; z < 16; z++) {
                    short index = this.getBlockIndex(x, y, z);
                    short blockStateId = chunkData.paletteStorage.getBlockAt(x, y, z);
                    short customBlockId = chunkData.customBlockPaletteStorage.getBlockAt(x, y, z);

                    if (blockStateId == 0 && customBlockId == 0) {
                        continue;
                    }
                    stream.writeShort(index);
                    stream.writeShort(blockStateId);
                    stream.writeShort(customBlockId);
                }
            }
        }
        return byteStream.toByteArray();
    }

    private Chunk deserializeChunk(int chunkX, int chunkZ, byte[] serialized) throws IOException {
        Chunk chunk = new DynamicChunk(this.createDefaultBiomeArray(), chunkX, chunkZ);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(serialized);
        DataInputStream stream = new DataInputStream(byteStream);

        while (stream.available() > 0) {
            short index = stream.readShort();
            short blockStateId = stream.readShort();
            short customBlockId = stream.readShort();

            BlockPosition position = this.indexBlock(index);
            chunk.UNSAFE_setBlock(position.getX(), position.getY(), position.getZ(), blockStateId, customBlockId, null, CustomBlockUtils.hasUpdate(customBlockId));
        }
        return chunk;
    }

    private Biome[] createDefaultBiomeArray() {
        Biome[] biomes = new Biome[1024];
        Arrays.fill(biomes, TITAN_BIOME);
        return biomes;
    }

    private short getBlockIndex(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        z = z % Chunk.CHUNK_SIZE_Z;

        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return (short) (index & 0xffff);
    }

    private BlockPosition indexBlock(int index) {
        int x = index & 0xF;
        int y = index >>> 4 & 0xFF;
        int z = index >> 12 & 0xF;
        return new BlockPosition(x, y, z);
    }
}
