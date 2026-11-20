package com.adjustclient.utilities.script.api;

public final class Vec2f {
    public float x;
    public float y;

    public Vec2f(float x, float y) {
    }

    public Vec2f() {
    }

    public Vec2f(float[] arr) {
    }

    public Vec2f(Vec2f vec) {
    }

    public native float squareDistance(float var1, float var2);

    public native float squareDistance(Vec2f var1);

    public native float squareDistanceVertical(float var1, float var2, float var3);

    public native float distance(float var1, float var2);

    public native float distance(Vec2f var1);

    public native float angleDistance(float var1, float var2);

    public native float angleDistance(Vec2f var1);

    public native boolean isInsideBounds(float var1, float var2, float var3, float var4);

    public native Vec2f subtract(Vec2f var1);

    public native Vec2f add(Vec2f var1);

    public native String toString();

    private native float $script$read$x();

    private native void $script$write$x(float var1);

    private native float $script$read$y();

    private native void $script$write$y(float var1);
}

