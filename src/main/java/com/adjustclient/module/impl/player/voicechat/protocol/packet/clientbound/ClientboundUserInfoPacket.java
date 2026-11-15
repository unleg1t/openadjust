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
import java.util.UUID;

public final class ClientboundUserInfoPacket extends ClientboundVoicePacket {
    public UUID userId;
    public JsonObject info;

    @Override public void handle(VoicePacketListener listener) {
        ((VoiceChatClient) listener).listener.onUserInfo(userId, info);
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        userId = VoicePacket.readUUID(in);
        info = JsonParser.parseString(VoicePacket.readString(StandardCharsets.UTF_8, in)).getAsJsonObject();
    }
}
