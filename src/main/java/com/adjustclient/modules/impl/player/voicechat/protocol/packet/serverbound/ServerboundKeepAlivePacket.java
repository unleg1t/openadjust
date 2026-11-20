package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH;
import java.io.ByteArrayOutputStream;

public final class ServerboundKeepAlivePacket extends ServerboundVoicePacket {
    public ServerboundKeepAlivePacket() { reliability = VoiceProtocolEnumH.UDP; }
    @Override public void write(ByteArrayOutputStream out, int version) {}
}
