package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientboundUserInfoListPacket extends ClientboundVoicePacket {
    public final List<UUID> userIds = new ArrayList<>();
    public final Map<UUID, JsonObject> infoByUser = new HashMap<>();

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        for (UUID userId : userIds) {
            JsonObject info = infoByUser.get(userId);
            if (info == null) info = new JsonObject();
            client.listener.onUserInfo(userId, info);
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = (byte) in.read();
        for (int i = 0; i < count; i++) userIds.add(VoicePacket.readUUID(in));
        int infoCount = (byte) in.read();
        for (int i = 0; i < infoCount; i++) {
            UUID userId = VoicePacket.readUUID(in);
            JsonObject info = JsonParser.parseString(VoicePacket.readString(StandardCharsets.UTF_8, in)).getAsJsonObject();
            infoByUser.put(userId, info);
        }
    }
}
