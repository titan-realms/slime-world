package net.titanrealms.slime.codec;

import net.minestom.server.instance.Instance;

public interface SlimeCodec {

    default Instance decodeSlime(byte[] slime) {
        return null;
    }

    default byte[] encodeInstance(Instance instance) {
        return new byte[0];
    }
}
