package com.adjustclient.modules.impl.render.trajectories.predictor;

import com.adjustclient.modules.impl.render.trajectories.TrajectoryPath;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.simulation.ArrowSimulation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.Vec3;

public final class HeldProjectilePredictor extends ThrowableProjectilePredictor {
    public HeldProjectilePredictor() { super(new ArrowSimulation()); }

    @Override protected ArrowSimulation initialize(EntityPlayer player, Vec3 position, Vec2f rotation, float partialTicks) {
        float velocity = 2.0F;
        if (player.isUsingItem()) {
            float duration = (72000.0F - player.getItemInUseCount() - 1.0F) + partialTicks;
            if (duration <= 0.0F) velocity = 0.0F;
            else {
                float charge = duration / 20.0F;
                float strength = (charge * charge + charge * 2.0F) / 3.0F;
                velocity = strength > 1.0F ? 2.0F : strength * 2.0F;
            }
        }
        ArrowSimulation arrow = (ArrowSimulation) simulation;
        arrow.initialize(player.worldObj, player.getEyeHeight(), position.xCoord, position.yCoord,
                position.zCoord, rotation.x, rotation.y, velocity);
        arrow.source = player;
        return arrow;
    }

    public TrajectoryPath predict(EntityArrow arrow) {
        ((ArrowSimulation) simulation).initialize(arrow);
        simulation.partialTicks = 1.0F;
        return simulate();
    }
}
