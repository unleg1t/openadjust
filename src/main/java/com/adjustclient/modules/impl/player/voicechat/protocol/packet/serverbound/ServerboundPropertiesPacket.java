package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.PropertyContainer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class ServerboundPropertiesPacket extends ServerboundVoicePacket {
    public final UUID target;
    public final PropertyContainer<?> properties;
    public ServerboundPropertiesPacket(UUID target, PropertyContainer<?> properties) { this.target = target; this.properties = properties; }
    @Override public void write(ByteArrayOutputStream out, int version) throws IOException {
        writeUUID(target, out);
        properties.write(out);
    }
}
