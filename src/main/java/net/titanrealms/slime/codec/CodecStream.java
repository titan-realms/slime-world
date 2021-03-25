package net.titanrealms.slime.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CodecStream {
    private ByteArrayOutputStream byteStreamOut;
    private ByteArrayInputStream byteStreamIn;
    private DataOutputStream dataStreamOut;
    private DataInputStream dataStreamIn;

    public static CodecStream create() {
        return new CodecStream();
    }

    public ByteArrayOutputStream byteOut() {
        if (this.byteStreamOut == null) {
            this.byteStreamOut = new ByteArrayOutputStream();
        }
        return this.byteStreamOut;
    }

    public ByteArrayInputStream byteIn(byte[] buf) {
        if (this.byteStreamIn == null) {
            this.byteStreamIn = new ByteArrayInputStream(buf);
        }
        return this.byteStreamIn;
    }

    public DataOutputStream out() {
        if (this.dataStreamOut == null) {
            this.dataStreamOut = new DataOutputStream(this.byteOut());
        }
        return this.dataStreamOut;
    }

    public DataInputStream in(byte[] buf) {
        if (this.dataStreamIn == null) {
            this.dataStreamIn = new DataInputStream(this.byteIn(buf));
        }
        return this.dataStreamIn;
    }
}
