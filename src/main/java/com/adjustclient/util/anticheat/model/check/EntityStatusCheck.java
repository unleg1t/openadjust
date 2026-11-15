package com.adjustclient.util.anticheat.model.check;
import com.adjustclient.util.anticheat.model.AntiCheatCheck;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
public final class EntityStatusCheck extends AntiCheatCheck {
    public Object field8;
    public boolean onPacket(Packet<?> packet) {
        return packet instanceof S3DPacketDisplayScoreboard
                && ((S3DPacketDisplayScoreboard) packet).getPosition() == 1;
    }
}
