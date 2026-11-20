package com.adjustclient.modules.impl.movement.antivoid;

import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.Vec3;

final class ClutchPlacement {
    final Vec2f input;
    final Vec3 position;
    final LegitSafeSpot preview;
    int remainingTicks;
    final int initialTicks;
    final int jumpDelay;
    final boolean jump;
    final boolean withinFallLimit;

    ClutchPlacement(Vec2f input, Vec3 position, LegitSafeSpot preview, int ticks, int jumpDelay,
                   boolean jump, boolean withinFallLimit) {
        this.input = input;
        this.position = position;
        this.preview = preview;
        this.remainingTicks = this.initialTicks = ticks;
        this.jumpDelay = jumpDelay;
        this.jump = jump;
        this.withinFallLimit = withinFallLimit;
    }
}
