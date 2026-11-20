package com.adjustclient.core.events;

public final class LightmapStateEvent extends Event {
    public boolean customColors;
    public boolean nightVision;

    public LightmapStateEvent(boolean customColors, boolean nightVision) {
        this.customColors = customColors;
        this.nightVision = nightVision;
    }
}
