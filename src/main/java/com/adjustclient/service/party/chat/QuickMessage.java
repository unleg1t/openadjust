package com.adjustclient.service.party.chat;

import java.util.List;

public abstract class QuickMessage {
    public final String name;
    public final String description;
    public static final List<QuickMessage> MESSAGES = new java.util.ArrayList<>(List.of(
            new UsernameQuickMessage("ign", null), new PositionQuickMessage("pos", null)));

    protected QuickMessage(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract String buildText();
}
