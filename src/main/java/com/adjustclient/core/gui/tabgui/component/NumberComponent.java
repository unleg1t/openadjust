package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.settings.NumberSetting;
import java.awt.Color;
import net.minecraft.util.ChatColor;
import com.adjustclient.core.gui.tabgui.SettingNode;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.gui.tabgui.TabGui;

public final class NumberComponent extends SettingNode<NumberSetting> {

    NumberComponent(String label, NumberSetting setting) {
        super(label, setting);
    }

    public NumberComponent(NumberSetting setting) {
        this(setting.getLabel(), setting);
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        if (!TabGui.layingOutNodes) {
            float target = 1.0F;
            if (this.hoverAnimation == null || System.currentTimeMillis() - this.hoverAnimation.lastCall >= 501L) {
                this.hoverAnimation = new Animation(Easing.LINEAR, 100L);
                this.hoverAnimation.value = target;
                this.hoverAnimation.restart();
            }
            int c = (int) (120.0F * this.hoverAnimation.update(target) + 120.0F);
            dl.color(new Color(c, c, c).getRGB());
        } else {
            dl.color(-1);
        }

        NumberSetting setting = this.setting;
        String valueText;
        if (setting.getFormatter() != null) {
            valueText = setting.getFormatter().transform(setting);
        } else {
            valueText = NumberSetting.FORMAT.format(((Number) setting.getValue()).doubleValue());
        }
        String text = this.label + ": " + ChatColor.BOLD + valueText;
        dl.text(Fonts.TAHOMA_10, text, x + 2.0F, y + 3.0F);
    }
}
