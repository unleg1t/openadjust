package com.adjustclient.module.impl.movement.speed;

import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.SimulationPlayer;
import net.minecraft.entity.Entity;

public abstract class SpeedMode<S extends SpeedState> implements MinecraftInstance, ModeOption {
    protected final SpeedModule module;
    public final com.adjustclient.anticheat.feature.Feature feature;
    protected SpeedMode(SpeedModule module, String name) {
        this.module = module;
        this.feature = new com.adjustclient.anticheat.feature.Feature(name, false);
    }
    protected SpeedMode(SpeedModule module, com.adjustclient.anticheat.feature.Feature feature) {
        this.module = module;
        this.feature = feature;
    }
    @Override public String getName() { return feature.name; }
    public abstract S createState(Entity entity);
    public abstract void update(S state);

    public void strafe(Entity entity, double speed) {
        double direction = (entity instanceof SimulationPlayer ? entity.rotationYaw : MovementUtil.getDirection())
                * 0.0174532925199433;
        entity.motionX = -Math.sin(direction) * speed;
        entity.motionZ = Math.cos(direction) * speed;
    }

    public boolean canRun() { return true; }
}
