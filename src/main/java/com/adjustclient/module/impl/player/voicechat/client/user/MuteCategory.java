package com.adjustclient.module.impl.player.voicechat.client.user;

public final class MuteCategory {
    public final String identifier;
    public MuteCategory(String identifier, float factor, long duration) { this.identifier = identifier; }
    public String toString() { return identifier; }
}
