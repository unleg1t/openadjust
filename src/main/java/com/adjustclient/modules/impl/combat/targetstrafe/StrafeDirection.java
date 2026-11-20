package com.adjustclient.modules.impl.combat.targetstrafe;
public enum StrafeDirection {
    LEFT(-90), RIGHT(90);
    public final int angle;
    StrafeDirection(int angle) { this.angle = angle; }
}
