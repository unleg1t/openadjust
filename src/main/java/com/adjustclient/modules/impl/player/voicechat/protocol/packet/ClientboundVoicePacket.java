package com.adjustclient.modules.impl.player.voicechat.protocol.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class ClientboundVoicePacket extends VoicePacket {
    public abstract void read(ByteArrayInputStream in, int version) throws IOException;
    public abstract void handle(VoicePacketListener listener);
}
