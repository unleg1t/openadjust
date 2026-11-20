package com.adjustclient.utilities.rotation;

public record AimPoint(net.minecraft.util.Vec3 hitVec, net.minecraft.util.Vec3 aimVec,
                       com.adjustclient.utilities.Vec2f angles, boolean directAim) {
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AimPoint that)) return false;
        return directAim == that.directAim && java.util.Objects.equals(angles, that.angles)
                && java.util.Objects.equals(aimVec, that.aimVec) && java.util.Objects.equals(hitVec, that.hitVec);
    }
    @Override public int hashCode() {
        int hash = java.util.Objects.hashCode(hitVec);
        hash = 31 * hash + java.util.Objects.hashCode(aimVec);
        hash = 31 * hash + (angles == null ? 0 : angles.hashCode());
        return 31 * hash + Boolean.hashCode(directAim);
    }
    @Override public String toString() {
        return "a[hitVec=" + hitVec + ", aimVec=" + aimVec + ", angles=" + angles
                + ", directAim=" + directAim + "]";
    }
}
