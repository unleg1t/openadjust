package com.adjustclient.core.events;

public class GuiOpenedEvent extends Event {
    public net.minecraft.client.gui.GuiScreen screen;

    public GuiOpenedEvent(net.minecraft.client.gui.GuiScreen screen) {
        this.screen = screen;
    }
}
