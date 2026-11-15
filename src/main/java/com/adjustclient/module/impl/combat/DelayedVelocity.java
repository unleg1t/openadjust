package com.adjustclient.module.impl.combat;

import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.util.Cooldown;
import net.minecraft.util.Vec3;

record DelayedVelocity(PacketHold blockRule, Cooldown stopwatch, Vec3 motion, boolean forced) {
    @Override public String toString() {
        return "k[blockRule=" + blockRule + ", stopwatch=" + stopwatch + ", motion=" + motion + ", forced=" + forced + "]";
    }
}
