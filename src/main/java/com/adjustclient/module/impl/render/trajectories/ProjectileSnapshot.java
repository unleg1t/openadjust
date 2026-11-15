package com.adjustclient.module.impl.render.trajectories;
import com.adjustclient.module.impl.render.trajectories.settings.*;
import com.adjustclient.setting.SettingContainer;
public final class ProjectileSnapshot {
    public final LocalTrajectorySettings local;
    public final PlayerTrajectorySettings players;
    public final PlayerTrajectorySettings friendly;
    public final ProjectileColorSettings projectileColors;
    public final ExplosionSettings entityHitBox;
    public ProjectileSnapshot(SettingContainer parent) {
        local = new LocalTrajectorySettings(parent);
        players = new PlayerTrajectorySettings(parent, "Players", 0xe84949);
        friendly = new PlayerTrajectorySettings(parent, "Friendly", 0x5b79b2);
        projectileColors = new ProjectileColorSettings(parent);
        entityHitBox = new ExplosionSettings(parent);
    }
}
