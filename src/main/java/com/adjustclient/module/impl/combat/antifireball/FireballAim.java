package com.adjustclient.module.impl.combat.antifireball;

import com.adjustclient.util.anticheat.entity.TrackedFireball;
import com.adjustclient.util.rotation.AimPoint;

final class FireballAim {
    final TrackedFireball fireball;
    final AimPoint aim;

    FireballAim(TrackedFireball fireball, AimPoint aim) {
        this.fireball = fireball;
        this.aim = aim;
    }
}
