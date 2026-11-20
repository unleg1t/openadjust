package com.adjustclient.modules.impl.player.voicechat.client.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceUserInfoCache {
    public final Map<UUID, VoiceUserInfo> users = new HashMap<>();
    public VoiceUserInfo getOrCreate(UUID userId) {
        VoiceUserInfo info = get(userId);
        if (info != null) return info;
        info = new VoiceUserInfo(userId);
        users.put(userId, info);
        return info;
    }
    public VoiceUserInfo get(UUID userId) { return users.get(userId); }
}
