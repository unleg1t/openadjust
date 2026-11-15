package com.adjustclient.module.impl.player.voicechat.client.session;

import java.util.UUID;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo;

public interface VoiceSessionListener {
    void onAuthenticated(boolean success);
    void onModeratorWarning(String message);
    void onDisconnect(com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState state, String reason);
    void onUserInfo(UUID userId, com.google.gson.JsonObject info);
    void onAudio(UUID senderId, byte[] data);
}
