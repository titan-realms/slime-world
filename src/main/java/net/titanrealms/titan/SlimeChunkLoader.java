package net.titanrealms.titan;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlimeChunkLoader implements IChunkLoader {

    @Override
    public boolean loadChunk(@NotNull Instance instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        return false;
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk, @Nullable Runnable callback) {

    }
}
