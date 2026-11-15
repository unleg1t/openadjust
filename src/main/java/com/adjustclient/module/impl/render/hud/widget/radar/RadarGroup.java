package com.adjustclient.module.impl.render.hud.widget.radar;

import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.NumberSetting;

import java.awt.Color;
import java.util.function.BooleanSupplier;

final class RadarGroup {

    final ExpandSetting group;
    final BooleanSetting enabled;
    final ColorSetting color;
    final NumberSetting alpha;
    final NumberSetting size;

    /**
     * @param parent         a2: the Radar's ExpandSetting
     * @param name           a3: group label ("Local", "Players", ...)
     * @param enabledDefault a4
     * @param color          a5: default colour
     * @param alpha          a6: default alpha (min 70, max 255, step 1)
     * @param size           7th (stack) argument: default dot size (min 1, max 3, step 1)
     */
    RadarGroup(ExpandSetting parent, String name, boolean enabledDefault, Color color, int alpha, int size) {
        this.group = new ExpandSetting(parent, name);
        this.enabled = new BooleanSetting(this.group, "Enabled", enabledDefault, "Yes", "No");
        this.group.enabledPredicate = (BooleanSupplier) () -> this.enabled.isEnabled();
        this.color = new ColorSetting(this.group, "Color", color, false, false, ColorSetting.Mode.PLAIN, null);
        this.alpha = new NumberSetting(this.group, "Alpha", alpha, 70, 255, 1);       // Integer cache entries 198 / 255 / 1
        this.size = new NumberSetting(this.group, "Size", size, 1, 3, 1);
    }

    int getColor() {
        return this.color.getColor().getRGB();
    }

    /** {@code ((Number) alpha.getValue()).intValue()} */
    int getAlpha() {
        return ((Number) this.alpha.getValue()).intValue();
    }

    /** {@code ((Number) size.getValue()).intValue()} */
    int getSize() {
        return ((Number) this.size.getValue()).intValue();
    }

    boolean isEnabled() {
        return this.enabled.isEnabled();
    }
}
