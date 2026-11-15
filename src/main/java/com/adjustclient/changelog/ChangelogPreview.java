package com.adjustclient.changelog;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.mainmenu.VoronoiBackground;
import com.adjustclient.util.ScreenUtil;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatColor;

public class ChangelogPreview {
    public String title;
    public String subtitle;
    public Animation hoverAnimation;
    public float width;
    public float height;
    public Object field24;

    public static final Runnable OPEN_SCREEN = () -> {
        open();
    };

    public ChangelogPreview() {
        long now = System.currentTimeMillis();
        title = String.valueOf(ChatColor.GRAY) + "Version from " + ChatColor.WHITE + Changelog.latest.title;
        if (Changelog.unreadCount != 0) subtitle = Changelog.unreadCount + " unread changes";
        width = Math.max(Fonts.TAHOMA_12.getStringWidth(title),
                subtitle == null ? 0.0F : Fonts.TAHOMA_10.getStringWidth(subtitle));
        height = subtitle == null ? 13.0F : 19.0F;
        hoverAnimation = new Animation(Easing.LINEAR, 100L, now);
    }

    public int getAlpha(double hoverWeight) {
        int alpha = (int) ((hoverAnimation.value * hoverWeight + 1.0 - hoverWeight) * 255.0);
        if (VoronoiBackground.transition != null) alpha = Math.min(alpha, (int) (VoronoiBackground.update() * 255.0F));
        return alpha;
    }

    public void render(int screenWidth, int screenHeight, float mouseX, float mouseY) {
        boolean hovered = mouseX > 0.0F && width >= mouseX
                && mouseY >= screenHeight - height && screenHeight >= mouseY
                && !ScreenUtil.isAdjustScreenOpen();
        hoverAnimation.update(hovered ? 1.0F : 0.0F);
        if (hovered) RenderUtil.cursor = 221188;
        Fonts.TAHOMA_12.drawString(title, 2.0F, screenHeight - 6.5F,
                new Color(210, 127, 59, getAlpha(0.44)).getRGB());
        if (subtitle != null) {
            Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            Color color = ColorUtil.withAlpha(ColorUtil.blend(accent, new Color(177, 177, 177), 0.4F), getAlpha(0.4));
            Fonts.TAHOMA_10.drawString(subtitle, 2.0F, screenHeight - 14.0F, color.getRGB());
        }
    }

    public static void open() {
        Minecraft.getMinecraft().displayGuiScreen(new ChangelogScreen());
    }
}
