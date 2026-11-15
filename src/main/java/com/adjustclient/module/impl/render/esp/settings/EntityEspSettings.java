package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.setting.*;

public class EntityEspSettings extends EspTargetSettings {
    public final BooleanSetting dormant;
    public final BooleanSetting invisible;
    public final BooleanSetting nametagsEnabled;
    public final ModeSetting<EspRectangleLayout> rectangles;
    public final BooleanSetting boldText;

    public EntityEspSettings(SettingContainer parent, String name, boolean enabled, boolean dormantDefault) {
        super(parent, name, enabled);
        dormant = new BooleanSetting(target, "Dormant", dormantDefault);
        dormant.setDescription("Display an older position of the entity if they are no longer in render distance.");
        invisible = new BooleanSetting(target, "Invisible", true);
        nametagsEnabled = new BooleanSetting(nametags, "Enabled", true, "Yes", "No");
        nametags.getSettings().remove(nametagColor);
        nametags.getSettings().remove(nametagScale);
        nametags.getSettings().remove(distance);
        nametags.getSettings().remove(nametagBind);
        nametags.getSettings().add(nametagColor);
        nametags.getSettings().add(nametagScale);
        nametags.getSettings().add(distance);
        nametags.getSettings().add(nametagBind);
        rectangles = new ModeSetting<>(nametags, "Rectangles", EspRectangleLayout.TOGETHER,
                () -> !background.isEnabled(), EspRectangleLayout.values());
        boldText = new BooleanSetting(nametags, "Bold text", false);
    }
}
