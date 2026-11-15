package com.adjustclient.module.impl.render.indicators;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.game.murdermystery.MurderMysteryGameState;
import com.adjustclient.hypixel.game.murdermystery.MurderMysteryPlayer;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.tag.PlayerTag;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.entity.TrackedEntity;
import com.adjustclient.util.anticheat.entity.TrackedProjectile;
import java.awt.Color;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatColor;

final class IndicatorEntry implements MinecraftInstance {
    public final TrackedEntity<?> entity;
    public final IndicatorSettings settings;
    public String label;
    public Color color = new Color(233, 235, 234);
    public String distanceLabel;
    public Color distanceColor = new Color(170, 170, 170);
    public Float health;
    public Color teamColor;
    public float nameX;
    public float distanceX;
    public int healthColor;
    public boolean teamColorChecked;
    public boolean enemy;

    IndicatorEntry(TrackedEntity<?> entity, IndicatorSettings settings) {
        this.entity = entity;
        this.settings = settings;
        update();
    }

    public void updateHealthColor() {
        if (!(settings instanceof LivingIndicatorSettings)) return;
        LivingIndicatorSettings livingSettings = (LivingIndicatorSettings) settings;
        if (!livingSettings.healthEnabled.isEnabled()) return;
        EntityLivingBase living = (EntityLivingBase) entity.getEntity();
        float maximum = living.getMaxHealth();
        health = Math.min(living.getHealth(), maximum) / maximum;
        healthColor = livingSettings.customColorEnabled.isEnabled()
                ? livingSettings.getHealthColor().getRGB()
                : Color.HSBtoRGB(health * 0.33333334F, 0.6F, 0.6F);
    }

    public void updateLabel() {
        if (settings.isNameEnabled()) {
            label = entity.getName();
            if (entity instanceof TrackedProjectile) {
                String shooter = entity instanceof com.adjustclient.util.anticheat.entity.TrackedFireball
                        ? ((com.adjustclient.util.anticheat.entity.TrackedFireball) entity).getShooterName()
                        : ((com.adjustclient.util.anticheat.entity.TrackedThrowable) entity).getShooterName();
                if (shooter != null) label += ChatColor.GRAY + " (" + ChatColor.WHITE
                        + shooter + ChatColor.GRAY + ")";
            }
            nameX = -Fonts.TAHOMA_10.getStringWidth(label) * 0.5F;
            if (settings instanceof PlayerIndicatorSettings
                    && ((PlayerIndicatorSettings) settings).nameTeamColor.isEnabled() && hasTeamColor()) color = teamColor;
            if (entity instanceof TrackedProjectile && ((TrackedProjectile<?>) entity).isDangerous()) {
                color = ColorUtil.blend(color, new Color(208, 69, 69),
                        ((TrackedProjectile<?>) entity).getImpactDistanceFactor());
            }
            if (enemy) color = new Color(210, 53, 53);
            if (Adjust.gameStateTracker.state instanceof MurderMysteryGameState) {
                MurderMysteryPlayer player = ((MurderMysteryGameState) Adjust.gameStateTracker.state).getPlayer(entity.getDisplayName());
                if (player != null) {
                    color = (Color) player.role.field16;
                    label = ChatColor.BOLD + label;
                }
            }
        }
        if (settings.isDistanceEnabled()) {
            distanceLabel = Math.round(mc.thePlayer.getDistanceToEntity(entity.getEntity())) + "m";
            distanceX = -Fonts.TAHOMA_8.getStringWidth(distanceLabel) * 0.5F;
            if (entity instanceof TrackedProjectile && ((TrackedProjectile<?>) entity).isDangerous()) {
                distanceColor = ColorUtil.blend(distanceColor, new Color(234, 93, 93),
                        ((TrackedProjectile<?>) entity).getImpactDistanceFactor());
            }
        }
    }

    public boolean hasTeamColor() {
        if (!teamColorChecked) {
            Integer rgb = WorldUtil.getTeamColor(entity.getEntity().getName());
            if (rgb != null) teamColor = new Color(rgb);
            teamColorChecked = true;
        }
        return teamColor != null;
    }

    public void update() {
        enemy = Adjust.INSTANCE.tagManager.getTag(entity.getDisplayName()) == PlayerTag.WHITELIST;
        updateLabel();
        updateHealthColor();
        if (settings instanceof PlayerIndicatorSettings && ((PlayerIndicatorSettings) settings).teamColor.isEnabled()) hasTeamColor();
    }
}
