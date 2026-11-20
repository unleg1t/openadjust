package com.adjustclient.modules.impl.combat.antifireball;

import com.adjustclient.utilities.anticheat.entity.TrackedFireball;
import com.adjustclient.utilities.rotation.AimPoint;

final class FireballAim {
    final TrackedFireball fireball;
    final AimPoint aim;

    FireballAim(TrackedFireball fireball, AimPoint aim) {
        this.fireball = fireball;
        this.aim = aim;
    }
}
