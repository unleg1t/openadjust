package com.adjustclient.core.gui.chat;

import net.minecraft.client.gui.GuiPersistentChat;
import java.util.function.Consumer;
public class ChatChannel {
    public String name;
    public GuiPersistentChat history;
    public Consumer<String> sender;
    public int sortOrder;

    public ChatChannel(String name) { this.name = name; }
    public ChatChannel(String name, GuiPersistentChat history, Consumer<String> sender, int order) {
        this.name = name;
        this.history = history;
        this.sender = sender;
        this.sortOrder = order;
    }
}
