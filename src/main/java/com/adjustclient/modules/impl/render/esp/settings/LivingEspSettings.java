package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.modules.impl.render.esp.EspPreviewSetting;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import java.awt.Color;
import net.minecraft.util.StringUtils;

public class LivingEspSettings extends BorderedEspSettings {
    public final BooleanSetting customName;
    public final BooleanSetting nametagHealthEnabled;
    public final BooleanSetting nametagHealthColored;
    public final BooleanSetting healthBarEnabled;
    public final BooleanSetting healthThinner;
    public final BooleanSetting customColorEnabled;
    public final ColorSetting healthColor;
    public final BooleanSetting outlineEnabled;
    public final ColorSetting outlineColor;
    private Object field116;

    public LivingEspSettings(SettingContainer parent, String name, boolean enabled, boolean dormant) {
        super(parent, name, enabled, dormant);
        EspPreviewSetting preview = new EspPreviewSetting(target, this);
        target.getSettings().remove(preview);
        target.getSettings().add(1, preview);
        customName = new BooleanSetting(nametags, "Custom name", true);
        ExpandSetting nametagHealth = new ExpandSetting(nametags, "Health");
        nametagHealthEnabled = new BooleanSetting(nametagHealth, "Enabled", true, "Yes", "No");
        nametagHealth.enabledPredicate = () -> nametagHealthEnabled.isEnabled();
        nametagHealthColored = new BooleanSetting(nametagHealth, "Colored", false);
        ExpandSetting health = new ExpandSetting(target, "Health");
        healthBarEnabled = new BooleanSetting(health, "Enabled", true, "Yes", "No");
        health.enabledPredicate = () -> healthBarEnabled.isEnabled();
        healthThinner = new BooleanSetting(health, "Thinner", false);
        ExpandSetting customColor = new ExpandSetting(health, "Custom color");
        customColorEnabled = new BooleanSetting(customColor, "Enabled", true, "Yes", "No");
        customColor.enabledPredicate = () -> customColorEnabled.isEnabled();
        healthColor = new ColorSetting(customColor, "Color", new Color(67, 90, 209), false, false, ColorSetting.Mode.PLAIN, null);
        ExpandSetting outline = new ExpandSetting(target, "Outline");
        outlineEnabled = new BooleanSetting(outline, "Enabled", false, "Yes", "No");
        outline.enabledPredicate = () -> outlineEnabled.isEnabled();
        outlineColor = new ColorSetting(outline, "Color", new Color(67, 90, 209), true, false, ColorSetting.Mode.PLAIN, null);
    }

    public int getHealthColor() {
        return healthColor.getColor().getRGB();
    }

    @Override public String getName(TrackedEntity<?> entity) {
        if (!customName.isEnabled()) return super.getName(entity);
        String name = entity.getEntity().getDisplayName().getFormattedText().trim();
        return entity.isRemoved() ? StringUtils.stripControlCodes(name) : name;
    }
}
