package net.titanrealms.slime.chunk;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.titanrealms.slime.models.SlimeChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlimeChunkLoader implements IChunkLoader {
    private final Map<Long, SlimeChunk> slimeChunks = new ConcurrentHashMap<>();

    @Override
    public boolean loadChunk(@NotNull Instance instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        return false;
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk, @Nullable Runnable callback) {

    }

    private SlimeChunk getSlimeChunk(int chunkX, int chunkZ) {
        return null;
    }
}
