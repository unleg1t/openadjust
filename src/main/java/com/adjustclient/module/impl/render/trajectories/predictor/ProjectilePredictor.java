package com.adjustclient.module.impl.render.trajectories.predictor;
import com.adjustclient.module.impl.render.trajectories.TrajectoryPath;
import com.adjustclient.module.impl.render.trajectories.settings.TrajectoryOwnerSettings;
import com.adjustclient.util.Vec2f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
public interface ProjectilePredictor {
    TrajectoryPath predict(EntityPlayer player, Vec3 position, Vec2f rotation, float partialTicks);
    void render(TrajectoryPath path, boolean translated, TrajectoryOwnerSettings settings, Integer override, int alpha, float partialTicks);
}
