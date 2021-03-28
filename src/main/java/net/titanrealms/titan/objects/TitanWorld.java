package net.titanrealms.titan.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TitanWorld {
    private final Map<String, byte[]> chunks = new ConcurrentHashMap<>();

    public byte[] getChunk(int x, int z) {
        return this.chunks.get(this.toKey(x, z));
    }

    public void setChunk(int x, int z, byte[] serialized) {
        this.chunks.put(this.toKey(x, z), serialized);
    }

    public void load() throws IOException {
        Path file = Paths.get("").resolve("test.titan");
        byte[] serialized = Files.readAllBytes(file);

        ByteArrayInputStream byteStream = new ByteArrayInputStream(serialized);
        DataInputStream stream = new DataInputStream(byteStream);
        while (stream.available() > 0) {
            int x = stream.readInt();
            int z = stream.readInt();
        }
    }

    // sloppy on purpose
    public void save() throws IOException {
        File file = Paths.get("").resolve("test.titan").toFile();
        file.mkdirs();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        for (Map.Entry<String, byte[]> chunk : this.chunks.entrySet()) {
            Vector2i position = this.fromKey(chunk.getKey());
            stream.writeInt(position.getX());
            stream.writeInt(position.getZ());
            stream.write(chunk.getValue());
        }
        Files.write(file.toPath(), byteStream.toByteArray());
    }

    private String toKey(int x, int z) {
        return x + "." + z;
    }

    private Vector2i fromKey(String key) {
        String[] args = key.split("\\.");
        return new Vector2i(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}
