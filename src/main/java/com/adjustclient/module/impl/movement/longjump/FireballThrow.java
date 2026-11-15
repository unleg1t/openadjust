package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.game.packet.PacketHold;
import net.minecraft.entity.projectile.EntityFireball;

final class FireballThrow {
    final PacketHold hold;
    final double motionY;
    final EntityFireball fireball;
    FireballThrow(PacketHold hold, double motionY, EntityFireball fireball) {
        this.hold = hold;
        this.motionY = motionY;
        this.fireball = fireball;
    }
}
