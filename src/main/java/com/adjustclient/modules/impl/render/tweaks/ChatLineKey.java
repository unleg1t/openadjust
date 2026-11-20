package com.adjustclient.modules.impl.render.tweaks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiPersistentChat;

final class ChatLineKey {
    public final GuiPersistentChat chat;
    public final Map<Integer, Set<ChatLine>> lines;
    public final Map<Integer, ChatStackEntry> counts;
    public Integer hash;
    public Object field20;

    ChatLineKey(GuiPersistentChat chat) {
        this.chat = chat;
        lines = new HashMap<>();
        counts = new HashMap<>();
    }
}
