package com.adjustclient.event;

public class GuiClosedEvent extends Event {
    public net.minecraft.client.gui.GuiScreen screen;

    public GuiClosedEvent(net.minecraft.client.gui.GuiScreen screen) {
        this.screen = screen;
    }
}
