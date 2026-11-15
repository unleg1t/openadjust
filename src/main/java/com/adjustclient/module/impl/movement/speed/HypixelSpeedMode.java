package com.adjustclient.module.impl.movement.speed;

import net.minecraft.entity.Entity;
import com.adjustclient.util.RandomUtil;

public final class HypixelSpeedMode extends SpeedMode<HypixelSpeedState> {
    static { RandomUtil.RANDOM.nextDouble(); }
    public HypixelSpeedMode(SpeedModule module) { super(module, "Hypixel"); }
    @Override public HypixelSpeedState createState(Entity entity) { return new HypixelSpeedState(entity); }
    @Override public void update(HypixelSpeedState state) { }
}
