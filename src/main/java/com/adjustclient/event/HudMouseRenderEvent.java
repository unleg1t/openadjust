package com.adjustclient.event;

public final class HudMouseRenderEvent extends Render2DEvent {
    public float mouseY;

    public HudMouseRenderEvent(net.minecraft.client.gui.ScaledResolution resolution, float mouseX, float mouseY) {
        super(resolution, mouseX);
        this.mouseY = mouseY;
    }

    public float getMouseX() {
        return partialTicks;
    }

    public void setMouseX(float mouseX) {
        partialTicks = mouseX;
    }
}
