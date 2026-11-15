package com.adjustclient.module.impl.combat.builder.feature;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindMode;
import com.adjustclient.module.impl.combat.builder.BuilderModule;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseFeature;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.util.GameStateUtil;

public abstract class BuilderFeature extends BuilderComponent {
    public final String name;
    public final ExpandSetting settings;
    public final Bind bind;
    public final BooleanSetting hold;
    public boolean active;

    protected BuilderFeature(BuilderModule owner, String name, String bindId, String bindDescription, boolean holdDefault) {
        super(owner);
        this.name = name;
        settings = new ExpandSetting(owner, name);
        hold = new BooleanSetting(settings, "Hold", holdDefault, "On", "Off");
        bind = Adjust.INSTANCE.bindManager.register(bindId, mode -> {
            if (hold.getValue() && !hold.isHidden()) {
                if (mode == BindMode.PRESSED && !active && owner.isEnabled() && canActivate()) start();
                if (mode == BindMode.RELEASED && active) deactivate();
            } else if (mode == BindMode.PRESSED && owner.isEnabled()
                    && mc.currentScreen == null && GameStateUtil.isActiveUnlessPlacing()) {
                if (active) deactivate();
                else start();
            }
        }, bindDescription);
        new KeybindSetting(settings, "Bind", bind);
        settings.addSetting(hold, 1);
    }

    public void activate() { }

    public void deactivate() {
        if (!active) return;
        owner.placer.target = null;
        owner.placer.placement = null;
        owner.placer.slot = -1;
        owner.placer.waitTicks = 0;
        active = false;
        if (this instanceof BedDefenseFeature) {
            BedDefenseFeature bed = (BedDefenseFeature) this;
            bed.bedSearchOrigin = null;
            bed.presetName = null;
            bed.movementTarget = null;
            bed.movementTicks = 0;
            bed.jumping = false;
            bed.jumpDirectionX = bed.jumpDirectionZ = 0;
        }
        owner.updateSuffix();
    }

    public static boolean canActivate() {
        if (mc.currentScreen != null) return false;
        if (ScaffoldModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.placeReady != null
                && ScaffoldModule.INSTANCE.placeReady) return false;
        return GameStateUtil.isActive();
    }

    public void start() {
        active = true;
        activate();
        owner.updateSuffix();
    }
}
