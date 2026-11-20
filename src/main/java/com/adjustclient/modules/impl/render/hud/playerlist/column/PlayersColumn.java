package com.adjustclient.modules.impl.render.hud.playerlist.column;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.List;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.settings.ColorSetting;
import java.awt.Color;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.util.ChatColor;

public final class PlayersColumn extends PlayerListColumn {

    @Override
    public String getName() {
        com.adjustclient.utilities.anticheat.player.PlayerTracker server = ServerTracker.INSTANCE.playerTracker;
        if (server == null) {
            return "Players";
        }
        return "Players " + ChatColor.GRAY + server.players.size();
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        Integer level = entry.level;
        String text;
        if (level == null) {
            if (StreamerModule.nameMap == null) {
                text = entry.name;
            } else {
                text = StreamerModule.replaceName(entry.name, entry.player.info.getGameProfile().getName());
            }
        } else if (StreamerModule.nameMap != null) {
            text = level + " " + entry.name.replace(entry.player.info.getGameProfile().getName(),
                    StreamerModule.replaceNames(entry.player.info.getGameProfile().getName()));
        } else {
            text = level + " " + entry.name;
        }
        float nameWidth = Fonts.TAHOMA_10.getStringWidth(text);
        float suffixWidth = Fonts.TAHOMA_8.getStringWidth(entry.suffix);
        return nameWidth + suffixWidth + 12.5F;
    }

    private float drawName(PlayerEntry entry, float x, float y) {
        float cx = x + 10.5F;
        com.adjustclient.utilities.hypixel.HypixelGame game = Adjust.gameStateTracker.state.game;             // current HypixelGame
        if (game == HypixelGame.BED_WARS) {
            if (entry.hypixelStats != null) {
                String stars = String.valueOf(entry.hypixelStats.getBedwarsStar());
                cx = Fonts.TAHOMA_10.drawString(stars + " ", cx, y, entry.getLevelColor()) - 0.5F;
            }
        } else if (entry.level != null) {
            cx = Fonts.TAHOMA_10.drawString(entry.level + " ", cx, y, entry.getLevelColor()) - 0.5F;
        }
        String name = entry.name;
        String profileName = entry.player.info.getGameProfile().getName();
        if (StreamerModule.nameMap != null) {                           // Streamer module
            name = StreamerModule.replaceName(name, profileName);
        }
        int color = entry.teamColor;
        if (entry.invisible && com.adjustclient.managers.config.user.UserConfiguration.field2f84 != null) {
            name = StringUtils.stripControlCodes(name);
        }
        if (entry.invisible && HUDModule.INSTANCE_ALT.playerList.selfHighlighting.isEnabled()) {
            Color hud = HUDModule.getHudColor();
            if (!HUDModule.isSecondaryActive() && HUDModule.colorSetting.getMode() == ColorSetting.Mode.PLAIN) {
                color = hud.getRGB();
            } else {
                String shown = StreamerModule.replaceNames(profileName);
                int index = name.indexOf(shown);
                if (index == -1) {
                    return RenderUtil.drawColoredString(Fonts.TAHOMA_10, name, hud.getRGB(), cx, y);
                }
                cx = Fonts.TAHOMA_10.drawString(name.substring(0, index), cx, y, hud.getRGB());
                return RenderUtil.drawColoredString(Fonts.TAHOMA_10, name.substring(index), hud.getRGB(), cx, y);
            }
        }
        if (entry.noEntity) {
            color = (((color >> 24) - 60) << 24) | (color & 0xFFFFFF);
        }
        String prefix = entry.npc ? ChatColor.STRIKETHROUGH.toString() : "";
        return Fonts.TAHOMA_10.drawString(prefix + name, cx, y, color);
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        deferred.add(new Runnable() {
            @Override
            public void run() {
                GL11.glScalef(0.2F, 0.2F, 0.2F);
                float sx = (x + 2.0F) * 5.0F;
                float sy = (y - 2.5F) * 5.0F;
                ResourceLocation skin = entry.player.info.getLocationSkin();
                if (skin == null) {
                    skin = DefaultPlayerSkin.getDefaultSkin(entry.player.info.getGameProfile().getId());
                }
                net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.drawHead(skin, sx, sy);           // draws the 8x8 head
                GL11.glScalef(5.0F, 5.0F, 5.0F);
            }
        });
        float end = this.drawName(entry, x, y);
        String suffix = entry.suffix;
        if (suffix != null && suffix.length() != 0) {
            Fonts.TAHOMA_8.drawString(suffix, end - 0.5F, y + 1.0F, entry.nameColor);
        }
    }

    @Override
    public boolean isToggleable() {
        return false;
    }
}
