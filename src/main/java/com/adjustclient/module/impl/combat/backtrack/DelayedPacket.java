package com.adjustclient.module.impl.combat.backtrack;

import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.util.Timer;
import net.minecraft.util.AxisAlignedBB;

final class DelayedPacket {
    final PacketHold hold;
    final Timer timer;
    final AxisAlignedBB box;
    DelayedPacket(PacketHold hold, AxisAlignedBB box) {
        this.hold = hold;
        this.box = box;
        this.timer = new Timer();
    }
}
