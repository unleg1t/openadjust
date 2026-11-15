package com.adjustclient.script.api;

public final class Vec3 {
    public double x;
    public double y;
    public double z;

    public Vec3() {
    }

    public Vec3(double x, double y, double z) {
    }

    public native Vec3 offset(double var1, double var3, double var5);

    public native Vec3 offset(Vec3 var1);

    public native double distanceTo(Vec3 var1);

    public native double distanceTo(double var1, double var3, double var5);

    public native Vec3 lerp(Vec3 var1, float var2);

    public native Vec3 subtractReverse(Vec3 var1);

    public native Vec3 normalize();

    public native Vec3 forward(double var1);

    public native Vec3 forward(double var1, float var3);

    public native double dotProduct(Vec3 var1);

    public native Vec3 crossProduct(Vec3 var1);

    public native Vec3 subtract(Vec3 var1);

    public native Vec3 subtract(double var1, double var3, double var5);

    public native Vec3 add(Vec3 var1);

    public native Vec3 addVector(double var1, double var3, double var5);

    public native Vec3 yCoord(double var1);

    public native double horizontalDistanceTo(Vec3 var1);

    public native double horizontalDistanceTo(double var1, double var3);

    public native double squareHorizontalDistanceTo(Vec3 var1);

    public native double horizontalHypotenuse();

    public native double squareDistanceTo(Vec3 var1);

    public native double squareDistanceTo(double var1, double var3, double var5);

    public native double lengthVector();

    public native Vec3 getIntermediateWithXValue(Vec3 var1, double var2);

    public native Vec3 getIntermediateWithYValue(Vec3 var1, double var2);

    public native Vec3 getIntermediateWithZValue(Vec3 var1, double var2);

    public native Vec3 rotatePitch(float var1);

    public native Vec3 rotateYaw(float var1);

    public native float getDirection();

    public native Vec3 invertHorizontally();

    public native Vec3 multiply(double var1);

    public native Vec3 horizontal();

    public native String toString();

    public native String toStringRounded();

    private native double $script$read$x();

    private native void $script$write$x(double var1);

    private native double $script$read$y();

    private native void $script$write$y(double var1);

    private native double $script$read$z();

    private native void $script$write$z(double var1);
}

