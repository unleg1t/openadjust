package com.adjustclient.modules.impl.render.hud.playerlist.column;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.LogoRenderer;
import com.adjustclient.core.rendering.font.svg.SvgImage;
import com.adjustclient.core.rendering.font.svg.SvgResources;
import com.adjustclient.managers.servertracker.FriendServerTracker;
import com.adjustclient.managers.user.ClientUser;
import net.minecraft.util.ChatColor;

public final class ClientColumn extends PlayerListColumn {

    public static final String NAME_MARKUP;
    /** new ResourceLocation("adjust", "svg/lunar_logo.svg") */
    public static final ResourceLocation LOGO;

    static {
        NAME_MARKUP = ChatColor.BOLD + "A" + ChatColor.WHITE + "djust";
        LOGO = new ResourceLocation("adjust", "svg/lunar_logo.svg");
    }

    @Override
    public String getName() {
        return "Client";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        String lowerName = entry.player.info.getGameProfile().getName().toLowerCase(Locale.getDefault());
        ClientUser user = Adjust.INSTANCE.screenStack.serverTracker.users.get(lowerName);
        if (user != null) {
            return 9.5F;
        }
        if (entry.invisible) {
            return 9.5F;
        }
        if (entry.customColor != null) {
            return 9.5F;
        }
        return 0.0F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        ClientUser user = Adjust.INSTANCE.screenStack.serverTracker.findUser(entry.player.info.getGameProfile().getName());
        boolean flag = entry.invisible;
        if (user == null && !flag) {
            com.adjustclient.utilities.lunar.websocket.LunarTabLogo c = entry.customColor;
            if (c != null) {
                SvgImage icon = SvgResources.get(LOGO);                     // loads / caches the svg
                Color color = new Color(c.red, c.green, c.blue, c.alpha);
                icon.draw(com.adjustclient.core.rendering.font.svg.FitMode.STRETCH, color.getRGB(),
                        x + 2.5F, y - 1.25F, 4.0F, 4.0F, 1.0F);
            }
            return;
        }
        Color first;
        Color second;
        if (flag) {
            // gradient from the HUD colour (animated colour if set, else the colour setting)
            Color hud = HUDModule.getHudColor();
            if (HUDModule.isSecondaryActive()) {
                first = hud;
                second = HUDModule.secondary.getColor();
            } else {
                first = hud;
                second = new Color((int) (hud.getRed() * 0.65F), (int) (hud.getGreen() * 0.65F), (int) (hud.getBlue() * 0.65F), hud.getAlpha());
            }
        } else {
            int rgb = user.getColor() | 0xFF000000;
            first = new Color(rgb, true);
            second = new Color((int) (first.getRed() * 0.65F), (int) (first.getGreen() * 0.65F), (int) (first.getBlue() * 0.65F), 255);
        }
        final float fx = x;
        final float fy = y;
        final Color c1 = first;
        final Color c2 = second;
        deferred.add(() -> LogoRenderer.draw(c1, c2, fx, fy - 3.0F, 8.0F, 8.0F));
    }
}
