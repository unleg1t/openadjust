package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.Timer;

final class TimedValue {

    final Timer timer;
    final net.minecraft.util.Vec3 value;
    final int duration;

    TimedValue(net.minecraft.util.Vec3 value, int duration) {
        this.timer = new Timer();   // lastMs = now
        this.value = value;
        this.duration = duration;
    }
}
