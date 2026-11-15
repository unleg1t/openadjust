package com.adjustclient.util;

public final class Vec2f {
    public float x;
    public float y;
    public Vec2f(float x, float y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != Vec2f.class) return false;
        Vec2f that = (Vec2f) other;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0;
    }
    @Override public int hashCode() { return java.util.Objects.hash(x, y); }
    @Override public String toString() { return com.google.common.base.Objects.toStringHelper(this).add("x", x).add("y", y).toString(); }
}
