package com.feminine.gender.client.render;

public final class BreastUVs {

    public static final UVQuad[] SKIN_LEFT = {
        new UVQuad(24, 21, 27, 26),
        new UVQuad(16, 21, 20, 26),
        new UVQuad(20, 17, 24, 21),
        new UVQuad(20, 25, 24, 27),
        new UVQuad(20, 21, 24, 26),
    };

    public static final UVQuad[] SKIN_RIGHT = {
        new UVQuad(28, 21, 32, 26),
        new UVQuad(21, 21, 24, 26),
        new UVQuad(24, 17, 28, 21),
        new UVQuad(24, 25, 28, 27),
        new UVQuad(24, 21, 28, 26),
    };

    public static final UVQuad[] OVERLAY_LEFT = {
        UVQuad.UNUSED,
        new UVQuad(17, 37, 20, 42),
        new UVQuad(20, 34, 24, 37),
        new UVQuad(20, 41, 24, 44),
        new UVQuad(20, 37, 24, 42),
    };

    public static final UVQuad[] OVERLAY_RIGHT = {
        new UVQuad(28, 37, 31, 42),
        UVQuad.UNUSED,
        new UVQuad(24, 34, 28, 37),
        new UVQuad(24, 41, 28, 44),
        new UVQuad(24, 37, 28, 42),
    };

    public static final UVQuad[] ARMOR_LEFT = {
        new UVQuad(24, 21, 28, 26),
        new UVQuad(16, 21, 20, 26),
        new UVQuad(20, 17, 24, 21),
        new UVQuad(20, 25, 24, 27),
        new UVQuad(20, 21, 24, 26),
    };

    public static final UVQuad[] ARMOR_RIGHT = {
        new UVQuad(28, 21, 32, 26),
        new UVQuad(20, 21, 24, 26),
        new UVQuad(24, 17, 28, 21),
        new UVQuad(24, 25, 28, 27),
        new UVQuad(24, 21, 28, 26),
    };

    private BreastUVs() {
    }
}
