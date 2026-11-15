package com.adjustclient.event;

public class GuiOpenedEvent extends Event {
    public net.minecraft.client.gui.GuiScreen screen;

    public GuiOpenedEvent(net.minecraft.client.gui.GuiScreen screen) {
        this.screen = screen;
    }
}
