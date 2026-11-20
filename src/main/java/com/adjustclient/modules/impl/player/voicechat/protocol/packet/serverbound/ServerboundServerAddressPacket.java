package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServerboundServerAddressPacket extends ServerboundVoicePacket {
    public final String address;
    public final int port;
    public ServerboundServerAddressPacket(String address, int port) { this.address = address; this.port = port; }
    @Override public void write(ByteArrayOutputStream out, int version) throws IOException {
        writeString(address, StandardCharsets.UTF_8, out);
        writeInt(port, out);
    }
}
