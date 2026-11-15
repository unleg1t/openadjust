package com.adjustclient.event;

public final class ScreenRender2DEvent extends Render2DEvent {
    public float mouseX;
    public float mouseY;

    public ScreenRender2DEvent(net.minecraft.client.gui.ScaledResolution resolution, float partialTicks, float mouseX, float mouseY) {
        super(resolution, partialTicks); this.mouseX = mouseX; this.mouseY = mouseY;
    }
}
