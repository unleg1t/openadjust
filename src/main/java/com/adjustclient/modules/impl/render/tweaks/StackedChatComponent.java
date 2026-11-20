package com.adjustclient.modules.impl.render.tweaks;

import net.minecraft.util.ChatComponentText;

public final class StackedChatComponent extends ChatComponentText {
    public StackedChatComponent(String text) { super(text); }
    @Override public int hashCode() { return 0; }
}
