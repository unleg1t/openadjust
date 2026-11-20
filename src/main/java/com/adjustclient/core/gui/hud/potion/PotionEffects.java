package com.adjustclient.core.gui.hud.potion;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.gui.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.adjustclient.core.rendering.NvgRenderer;

public final class PotionEffects extends HudElement {

    private NvgRenderer list;
    private List<PotionEntry> entries;
    private Animation shift;
    private Object field20;

    public PotionEffects() {
        this.movable = com.adjustclient.core.gui.hud.Anchor.BOTTOM_RIGHT;
        this.list = new NvgRenderer();
        this.entries = new ArrayList<>();
        this.shift = new Animation(Easing.EASE_OUT_CUBIC, 500L);
        this.shift.value = -2.0F;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** Total text width of an entry (name [+ " " + level] in Tahoma 12 plus the duration text in Tahoma 10). */
    private static float getWidth(PotionEntry entry) {
        String text = entry.levelText != null ? String.format("%s%s%s", entry.name, " ", entry.levelText) : entry.name;
        return Fonts.TAHOMA_12.getStringWidth(text) + Fonts.TAHOMA_10.getStringWidth(entry.durationText);
    }

    @Override
    public NvgRenderer build(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        this.list.clear();
        float scale = (float) sr.getScaleFactor_double();
        float target = mc.currentScreen instanceof GuiChat ? 12.5F / Math.max(1.0F, scale - 1.0F) : -1.0F;
        this.shift.update(target);

        int index = 0;
        for (PotionEntry entry : this.entries) {
            String text = entry.levelText != null ? String.format("%s%s%s", entry.name, " ", entry.levelText) : entry.name;
            this.list.color = (entry.beneficial ? 150 : 220) << 24 | Potion.potionTypes[entry.potionId].getLiquidColor();

            float durationBlock = 0.0F;
            if (entry.durationText != null) {
                durationBlock = Fonts.TAHOMA_10.getStringWidth(entry.durationText) + 1.0F;
            }
            float nameWidth = Fonts.TAHOMA_12.getStringWidth(text);
            this.list.text(Fonts.TAHOMA_12, "", (-nameWidth - durationBlock) - 2.0F, -this.shift.getValue() - 2.0F);
            float y = this.shift.getValue() + (float) index * 6.5F;

            // name, right aligned to the left of the duration (2.0 from the right edge of the list)
            this.list.text(Fonts.TAHOMA_12, text, (-nameWidth - durationBlock) - 2.0F, y);

            if (entry.durationText != null) {
                this.list.color = entry.color;                // duration text colour (white -> red)
                this.list.text(Fonts.TAHOMA_10, entry.durationText, (1.0F - durationBlock) - 2.0F, y + 1.0F);
            }
            index++;
        }
        return this.list;
    }

    @Override
    public void update() {
        this.entries.removeIf(entry -> {
            for (PotionEffect effect : Minecraft.getMinecraft().thePlayer.getActivePotionEffects()) {
                if (effect.getPotionID() == entry.potionId) {
                    return false;
                }
            }
            return true;
        });

        for (PotionEffect effect : Minecraft.getMinecraft().thePlayer.getActivePotionEffects()) {
            PotionEntry existing = null;
            for (PotionEntry candidate : this.entries) {
                if (candidate.potionId == effect.getPotionID()) {
                    existing = candidate;
                    break;
                }
            }
            if (existing == null) {
                Potion potion = Potion.potionTypes[effect.getPotionID()];
                existing = new PotionEntry();
                existing.name = I18n.format(potion.getName());
                existing.potionId = effect.getPotionID();
                existing.beneficial = !potion.isBadEffect();
                existing.fade = new Animation(Easing.EASE_OUT_SINE, 350L);   // started now
                existing.fade.value = 1.0F;                               // initial value
                this.entries.add(existing);
            }
            existing.update(effect.getAmplifier(), effect.getDuration(), effect.getIsPotionDurationMax());
        }

        this.entries.sort(Comparator.comparingDouble(PotionEffects::getWidth));
    }
}
