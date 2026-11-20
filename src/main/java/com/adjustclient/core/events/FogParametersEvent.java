package com.adjustclient.core.events;

public final class FogParametersEvent extends Event {
    public float farPlaneDistance;
    public float density;
    public float start;
    public float end;
    public int mode;
    public boolean radial;
    public boolean fogStandard;

    public FogParametersEvent(float farPlaneDistance, float density, float start, float end,
            int mode, boolean radial, boolean fogStandard) {
        this.farPlaneDistance = farPlaneDistance;
        this.density = density;
        this.start = start;
        this.end = end;
        this.mode = mode;
        this.radial = radial;
        this.fogStandard = fogStandard;
    }
}
