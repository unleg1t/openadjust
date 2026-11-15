package com.adjustclient.module.impl.player.voicechat.client.session;

import java.util.UUID;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo;

public class NoOpVoiceSessionListener implements VoiceSessionListener {
    public void onAuthenticated(boolean success) {}
    public void onModeratorWarning(String message) {}
    public void onDisconnect(com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState state, String reason) {}
    public void onUserInfo(UUID userId, com.google.gson.JsonObject info) {}
    public void onAudio(UUID senderId, byte[] data) {}
}
