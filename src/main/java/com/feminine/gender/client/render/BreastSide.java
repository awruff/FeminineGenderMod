package com.feminine.gender.client.render;

public enum BreastSide {
    LEFT(true),
    RIGHT(false);

    private final boolean left;

    BreastSide(boolean left) {
        this.left = left;
    }

    public boolean isLeft() {
        return left;
    }

    public float leftOrNegate(float value) {
        return left ? value : -value;
    }

    public float forSide(float leftValue, float rightValue) {
        return left ? leftValue : rightValue;
    }
}
