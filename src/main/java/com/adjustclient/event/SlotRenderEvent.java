package com.adjustclient.event;

public final class SlotRenderEvent extends Event {
    public net.minecraft.inventory.Slot slot;
    public net.minecraft.client.gui.ScaledResolution scaledResolution;
    public net.minecraft.client.gui.inventory.GuiContainer screen;
    public int guiLeft;
    public int guiTop;
    public int mouseX;
    public int mouseY;

    public SlotRenderEvent(net.minecraft.inventory.Slot slot, net.minecraft.client.gui.ScaledResolution scaledResolution,
            net.minecraft.client.gui.inventory.GuiContainer screen, int guiLeft, int guiTop, int mouseX, int mouseY) {
        this.slot = slot;
        this.scaledResolution = scaledResolution;
        this.screen = screen;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
