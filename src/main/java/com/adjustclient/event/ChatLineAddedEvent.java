package com.adjustclient.event;

public final class ChatLineAddedEvent extends Event {
    public net.minecraft.client.gui.GuiPersistentChat chat;
    public net.minecraft.client.gui.ChatLine chatLine;

    public ChatLineAddedEvent(net.minecraft.client.gui.GuiPersistentChat chat, net.minecraft.client.gui.ChatLine chatLine) {
        this.chat = chat;
        this.chatLine = chatLine;
    }
}
