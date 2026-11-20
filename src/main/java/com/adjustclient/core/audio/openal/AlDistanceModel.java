package com.adjustclient.core.audio.openal;

public enum AlDistanceModel {
    NONE(0), INVERSE(53249), INVERSE_CLAMPED(53250), LINEAR(53251), LINEAR_CLAMPED(53252), EXPONENT(53253), EXPONENT_CLAMPED(53254);
    public final int value;
    AlDistanceModel(int value) { this.value = value; }
    public String field20;
}
