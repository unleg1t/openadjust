package com.adjustclient.managers.party.chat;

import net.minecraft.client.Minecraft;

public class UsernameQuickMessage extends QuickMessage {
    public UsernameQuickMessage(String name, String description) { super(name, description); }

    @Override public String buildText() {
        return Minecraft.getMinecraft().getSession().getUsername();
    }
}
