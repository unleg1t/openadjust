package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.Adjust;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import java.awt.Color;

public class EspTargetSettings {
    public final ExpandSetting target;
    public final BooleanSetting enabled;
    public final BooleanSetting useFov;
    public final NumberSetting maxAlpha;
    public final BooleanSetting background;
    public final ExpandSetting information;
    public final ExpandSetting nametags;
    public ColorSetting nametagColor;
    public final NumberSetting nametagScale;
    public final BooleanSetting distance;
    public final KeybindSetting nametagBind;

    public EspTargetSettings(SettingContainer parent, String name, boolean enabledDefault) {
        target = new ExpandSetting(parent, name);
        enabled = new BooleanSetting(target, "Enabled", enabledDefault, "Yes", "No");
        target.enabledPredicate = () -> enabled.isEnabled();
        ExpandSetting alpha = new ExpandSetting(target, "Alpha");
        maxAlpha = new NumberSetting(alpha, "Max. alpha", 255, 70, 255, 1);
        useFov = new BooleanSetting(alpha, "Use FOV", false);
        // Both predicates capture the derived EntityEspSettings nametag Enabled setting.
        information = new ExpandSetting(target, "Information");
        information.enabledPredicate = () -> ((EntityEspSettings) this).nametagsEnabled.isEnabled();
        nametags = new ExpandSetting(information, "Nametags");
        nametags.enabledPredicate = () -> ((EntityEspSettings) this).nametagsEnabled.isEnabled();
        nametagColor = new ColorSetting(nametags, "Color", new Color(233, 235, 234), false, false, ColorSetting.Mode.PLAIN, null);
        nametagScale = new NumberSetting(nametags, "Scale", 1.0F, 1.0F, 4.0F, 0.05F);
        distance = new BooleanSetting(nametags, "Distance", false);
        String id = "esp.nametags." + name.toLowerCase().replace(' ', '_'); // StringUTF16/Latin1.replace(' ', '_')
        nametagBind = new KeybindSetting(nametags, "Bind", Adjust.INSTANCE.bindManager.register(id, null, name + " Nametags"));
        background = new BooleanSetting(information, "Background", true);
    }

    public float getNametagScale() { return nametagScale.getFloat(); }

    public String getName(TrackedEntity<?> entity) { return entity.getDisplayName(); }
}
