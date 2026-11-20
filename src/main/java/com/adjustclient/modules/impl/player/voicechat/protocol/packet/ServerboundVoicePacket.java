package com.adjustclient.modules.impl.player.voicechat.protocol.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ServerboundVoicePacket extends VoicePacket {
    public abstract void write(ByteArrayOutputStream out, int version) throws IOException;
}
