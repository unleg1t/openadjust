package com.adjustclient.module.impl.render.trajectories.settings;

import com.adjustclient.setting.*;
import java.awt.Color;
import java.util.function.BooleanSupplier;

public abstract class TrajectoryOwnerSettings {
    public final ExpandSetting group;
    public final BooleanSetting enabled;
    public final ColorSetting color;
    public final ColorSetting hitColor;
    public final BooleanSetting fireballsEnabled;
    public final BooleanSetting explosionEnabled;
    public final ColorSetting explosionColor;
    public final BooleanSetting explosionOutline;
    public final ColorSetting explosionOutlineColor;

    protected TrajectoryOwnerSettings(SettingContainer parent, String name, int rgb) {
        group = new ExpandSetting(parent, name);
        enabled = new BooleanSetting(group, "Enabled", true);
        BooleanSupplier ownerHidden = () -> !enabled.isEnabled();
        group.enabledPredicate = ownerHidden;
        color = new ColorSetting(group, "Color", new Color(0xaa000000 | rgb, true));
        hitColor = new ColorSetting(group, "Hit Color", new Color(0x82000000 | rgb, true));
        ExpandSetting fireballs = new ExpandSetting(group, "Fireballs");
        fireballsEnabled = new BooleanSetting(fireballs, "Enabled", true);
        BooleanSupplier fireballsHidden = () -> !fireballsEnabled.isEnabled();
        fireballs.enabledPredicate = fireballsHidden;
        ExpandSetting explosion = new ExpandSetting(fireballs, "Explosion");
        explosionEnabled = new BooleanSetting(explosion, "Enabled", false);
        explosionEnabled.setDescription("Simulate the blast and highlight the blocks and entities it would hit.");
        BooleanSupplier explosionHidden = () -> !explosionEnabled.isEnabled();
        explosion.enabledPredicate = explosionHidden;
        explosionColor = new ColorSetting(explosion, "Color", new Color(0x46000000 | rgb, true));
        explosionOutline = new BooleanSetting(explosion, "Outline", true);
        explosionOutlineColor = new ColorSetting(explosion, "Outline Color", new Color(0x64000000 | rgb, true));
    }
    public int getExplosionOutlineColor() { return explosionOutlineColor.getColor().getRGB(); }
    public int getExplosionColor() { return explosionColor.getColor().getRGB(); }
}
