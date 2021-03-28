package net.titanrealms.titan.objects;

// immutable
public class Vector2i {
    private final int x;
    private final int z;

    public Vector2i(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}
