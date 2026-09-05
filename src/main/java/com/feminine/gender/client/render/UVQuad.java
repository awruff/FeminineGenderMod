package com.feminine.gender.client.render;

public final class UVQuad {

    public static final UVQuad UNUSED = new UVQuad(0, 0, 0, 0);

    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    public UVQuad(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    boolean isUnused() {
        return x1 == 0 && y1 == 0 && x2 == 0 && y2 == 0;
    }
}
