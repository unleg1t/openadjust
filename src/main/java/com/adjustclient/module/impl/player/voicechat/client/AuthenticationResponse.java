package com.adjustclient.module.impl.player.voicechat.client;

public final class AuthenticationResponse {
    public final String string;
    public final Object player;
    public AuthenticationResponse(String string, Object player) { this.string = string; this.player = player; }
    public String toString() { return "AuthenticationResponse(string=" + string + ", player=" + player + ")"; }
}
