package com.adjustclient.modules.impl.movement.speed;

import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.SimulationPlayer;
import net.minecraft.entity.Entity;

public abstract class SpeedMode<S extends SpeedState> implements MinecraftInstance, ModeOption {
    protected final SpeedModule module;
    public final com.adjustclient.utilities.anticheat.feature.Feature feature;
    protected SpeedMode(SpeedModule module, String name) {
        this.module = module;
        this.feature = new com.adjustclient.utilities.anticheat.feature.Feature(name, false);
    }
    protected SpeedMode(SpeedModule module, com.adjustclient.utilities.anticheat.feature.Feature feature) {
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
