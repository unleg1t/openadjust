package com.adjustclient.module.impl.combat.displacement;

record DisplacementResult(int voidTick, int fallTick, boolean hitWall, boolean staysOverVoid, boolean staysOverDrop) {
    @Override public String toString() {
        return "c[firstVoidTick=" + voidTick + ", firstAirTick=" + fallTick
                + ", collidedHorizontally=" + hitWall + ", staysOverVoid=" + staysOverVoid
                + ", staysOverAir=" + staysOverDrop + "]";
    }
}
