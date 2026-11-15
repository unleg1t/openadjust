package com.adjustclient.ui.hud.potion;

import com.adjustclient.render.animation.Animation;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;

import java.awt.Color;
import com.adjustclient.util.StringUtil;

public final class PotionEntry {

    public String name;
    public String durationText;
    public String levelText;
    public Animation fade;
    public int color;
    public int potionId;
    public boolean beneficial;
    public Object field32;

    /**
     * @param amplifier PotionEffect.getAmplifier()   (a2)
     * @param duration  PotionEffect.getDuration()     (a3, ticks)
     * @param maxed     PotionEffect.getIsPotionDurationMax()  (a4)
     */
    public void update(int amplifier, int duration, boolean maxed) {
        if (duration >= 0x7FFF) {
            this.durationText = null;
        } else if (maxed) {
            this.durationText = null;
        } else {
            this.durationText = StringUtils.ticksToElapsedTime(duration);     // net.minecraft.util.StringUtils.a
        }
        if (amplifier >= 1) {
            this.levelText = StringUtil.toRoman(amplifier + 1);          // roman numeral
        } else {
            this.levelText = null;
        }

        // blend between red (204, 85, 85) and white, alpha 150 (beneficial) / 220 (bad)
        float target = duration >= 260 ? 1.0F : 0.0F;
        this.fade.update(target);
        int alpha = this.beneficial ? 150 : 220;
        float t = this.fade.getValue();
        if (t > 1.0F) {
            t = 1.0F;
        }
        if (t < 0.0F) {
            this.color = new Color(204, 85, 85, alpha).getRGB();
        } else {
            float u = 1.0F - t;
            int r = (int) (u * 204.0F + 255.0F * t);
            int g = (int) (u * 85.0F + 255.0F * t);
            int b = (int) (u * 85.0F + 255.0F * t);
            int a = (int) (alpha * u + alpha * t);
            this.color = new Color(r, g, b, a).getRGB();     // java.awt.Color.testColorValueRange
        }
    }
}
