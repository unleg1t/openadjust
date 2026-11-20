package com.adjustclient.modules.impl.movement.speed;

import net.minecraft.entity.Entity;

public final class MinibloxSpeedMode extends SpeedMode<MinibloxSpeedState> {
    private Object field12;
    public MinibloxSpeedMode(SpeedModule module) { super(module, "Miniblox"); }
    @Override public MinibloxSpeedState createState(Entity entity) { return new MinibloxSpeedState(entity); }
    @Override public void update(MinibloxSpeedState state) { }
}
