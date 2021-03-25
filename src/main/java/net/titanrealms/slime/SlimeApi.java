package net.titanrealms.slime;

import net.titanrealms.slime.codec.SlimeCodec;
import net.titanrealms.slime.codec.SlimeCodecImpl;

public class SlimeApi {
    private final SlimeCodec codec = new SlimeCodecImpl();

    private static SlimeApi instance;

    public static SlimeApi instance() {
        if (instance == null) {
            instance = new SlimeApi();
        }
        return instance;
    }

    public SlimeCodec getCodec() {
        return this.codec;
    }
}
