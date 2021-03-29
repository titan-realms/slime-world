package net.titanrealms.titan.objects;

import net.titanrealms.titan.TitanChunkLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TitanWorld {
    private final Map<String, byte[]> chunks = new ConcurrentHashMap<>();
    private final TitanChunkLoader chunkLoader = new TitanChunkLoader(this);

    public TitanChunkLoader getChunkLoader() {
        return this.chunkLoader;
    }

    public byte[] getChunk(int x, int z) {
        return this.chunks.get(this.toKey(x, z));
    }

    public void setChunk(int x, int z, byte[] serialized) {
        this.chunks.put(this.toKey(x, z), serialized);
    }

    public byte[] serializeWorld() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        for (Map.Entry<String, byte[]> chunk : this.chunks.entrySet()) {
            Vector2i position = this.fromKey(chunk.getKey());
            stream.writeInt(position.getX());
            stream.writeInt(position.getZ());
            stream.write(chunk.getValue());
        }
        return byteStream.toByteArray();
    }

    public static TitanWorld deserializeWorld(byte[] serialized) throws IOException {
        TitanWorld world = new TitanWorld();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(serialized);
        DataInputStream stream = new DataInputStream(byteStream);
        while (stream.available() > 0) {
            int x = stream.readInt();
            int z = stream.readInt();

            ByteArrayOutputStream builderByteStream = new ByteArrayOutputStream();
            DataOutputStream builderStream = new DataOutputStream(builderByteStream);
            int blockLength = stream.readInt();
            builderStream.writeInt(blockLength);
            for (int i = 0; i < blockLength; i++) {
                short index = stream.readShort();
                short blockStateId = stream.readShort();
                short customBlockId = stream.readShort();
                builderStream.writeShort(index);
                builderStream.writeShort(blockStateId);
                builderStream.writeShort(customBlockId);
            }
            world.setChunk(x, z, builderByteStream.toByteArray());
        }
        return world;
    }

    public static TitanWorld loadFromFile(Path path, String name) {
        try {
            path = path.resolve(name.concat(".titan"));
            if (!Files.exists(path)) {
                return null;
            }
            RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
            byte[] serialized = new byte[(int) file.length()];
            file.seek(0);
            file.readFully(serialized);
            file.close();
            return deserializeWorld(serialized);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // sloppy on purpose
    public void saveAsFile(Path path, String name) {
        try {
            path = path.resolve(name.concat(".titan"));
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
            file.seek(0);
            file.setLength(0);
            file.write(this.serializeWorld());
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String toKey(int x, int z) {
        return x + "." + z;
    }

    private Vector2i fromKey(String key) {
        String[] args = key.split("\\.");
        return new Vector2i(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}
