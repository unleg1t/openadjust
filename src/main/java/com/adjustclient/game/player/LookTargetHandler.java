package com.adjustclient.game.player;

import com.adjustclient.event.Listener;
import com.adjustclient.util.MinecraftInstance;

public final class LookTargetHandler implements Listener, MinecraftInstance {
    public LookTarget target;
    private Object field8;

    public static final class LookTarget {
        public Object field4;
        public Object field8;
        public Object field12;
        public Object field16;

        public LookTarget() {
        }

        public LookTarget(net.minecraft.entity.Entity entity, int duration, net.minecraft.util.Vec3 position, float speed) {
            this.field4 = entity;
            this.field8 = position;
            this.field12 = new com.adjustclient.util.Timer();
        }
    }
}
