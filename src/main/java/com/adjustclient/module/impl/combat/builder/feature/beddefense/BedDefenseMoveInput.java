package com.adjustclient.module.impl.combat.builder.feature.beddefense;

record BedDefenseMoveInput(float forward, float strafe, double directionX, double directionZ) {
    @Override public String toString() {
        return "e[forward=" + forward + ", strafe=" + strafe + ", directionX=" + directionX
                + ", directionZ=" + directionZ + "]";
    }
}
