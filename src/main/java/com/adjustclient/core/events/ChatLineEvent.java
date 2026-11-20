package com.adjustclient.core.events;

public final class ChatLineEvent extends Event {
    public net.minecraft.client.gui.GuiPersistentChat chat;
    public net.minecraft.util.IChatComponent component;
    public boolean displayOnly;
    public boolean pre;
    public boolean removedOldLine;
    public long timestamp;

    public ChatLineEvent(net.minecraft.client.gui.GuiPersistentChat chat, net.minecraft.util.IChatComponent component,
            boolean displayOnly, boolean pre, boolean removedOldLine, long timestamp) {
        this.chat = chat;
        this.component = component;
        this.displayOnly = displayOnly;
        this.pre = pre;
        this.removedOldLine = removedOldLine;
        this.timestamp = timestamp;
    }
}
