package com.adjustclient.module.impl.render.trajectories.settings;
import com.adjustclient.module.impl.render.trajectories.ProjectileType;
import com.adjustclient.setting.*;
import java.util.EnumMap;
public final class PlayerTrajectorySettings extends TrajectoryOwnerSettings {
    public final EnumMap<ProjectileType, BooleanSetting> heldProjectiles;
    public final BooleanSetting fovAlpha;
    public PlayerTrajectorySettings(SettingContainer parent, String name, int rgb) {
        super(parent, name, rgb);
        ExpandSetting held = new ExpandSetting(group, "Held Projectiles");
        held.setDescription("Show predicted trajectories for projectiles other players are holding or aiming.");
        heldProjectiles = new EnumMap<>(ProjectileType.class);
        for (ProjectileType type : ProjectileType.values())
            heldProjectiles.put(type, new BooleanSetting(held, type.displayName, true));
        fovAlpha = new BooleanSetting(group, "FOV Alpha", false);
        fovAlpha.setDescription("Increases the opacity of renders the closer the player looks at you.");
    }
}
