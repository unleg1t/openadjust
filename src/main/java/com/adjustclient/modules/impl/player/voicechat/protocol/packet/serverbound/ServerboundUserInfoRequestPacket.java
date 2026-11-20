package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public final class ServerboundUserInfoRequestPacket extends ServerboundVoicePacket {
    public final ArrayList<UUID> userIds = new ArrayList<>();
    public final int requestId;
    public ServerboundUserInfoRequestPacket(int requestId) { this.requestId = requestId; }
    @Override public void write(ByteArrayOutputStream out, int version) {
        out.write(requestId);
        writeVarInt(userIds.size(), out);
        for (UUID id : userIds) writeUUID(id, out);
    }
}
