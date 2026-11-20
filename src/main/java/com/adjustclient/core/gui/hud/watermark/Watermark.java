package com.adjustclient.core.gui.hud.watermark;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.gui.hud.HudElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;

import java.awt.Color;
import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.render.hud.HUDModule;

public final class Watermark extends HudElement {

    public Watermark() {
        this.movable = com.adjustclient.core.gui.hud.Anchor.TOP_LEFT;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void render(ScaledResolution sr) {
        HUDModule hud = HUDModule.INSTANCE_ALT;
        if (!hud.watermark.isEnabled()) {
            return;
        }

        String clientName = Adjust.NAME;
        if (clientName.isEmpty()) {
            return;
        }

        NvgFontRenderer font = Fonts.TAHOMA_14;
        float y = 2.5F;

        if (ServiceUtil.ahmedElSisiMode) {
            float x = font.drawString(ChatColor.RESET + "مح" + ChatColor.WHITE + "ارب الله" + ChatColor.RED, 2.5F, y, HUDModule.getHudColor().getRGB());
            ServiceUtil.ahmedElSisiMode = false;
            Fonts.TAHOMA_14.drawString(ChatColor.BOLD + "Danger Edition 0.7D", x, y, new Color(223, 56, 39).getRGB());
            ServiceUtil.ahmedElSisiMode = true;
            return;
        }

        String first = ChatColor.BOLD + String.valueOf(clientName.charAt(0));
        Color hudColor = HUDModule.getHudColor();
        float x = 2.5F;
        x = font.drawString(first, x, y, hudColor.getRGB());
        if (clientName.length() >= 2) {
            x = Fonts.TAHOMA_14.drawString(clientName.substring(1), x, y, new Color(225, 228, 228).getRGB());
        }

        String edition = getEdition(Adjust.username);
        if (edition != null) {
            x += 1.0F;
            Fonts.TAHOMA_10.drawString(edition, x, y, new Color(170, 170, 170).getRGB());
        }
    }

    private static String getEdition(String username) {
        if (username == null) return null;
        switch (username) {
            case "Admin":
            case "axyo":
            case "KCALS":
            case "Sexting":
            case "tofle":
            case "xTori":
                return "KCALS Edition";
            case "auth":
            case "Fatal":
            case "purity":
            case "wykt":
                return "Nonce Edition";
            case "billionaire":
                return "Tuk Tuk Tuk Sahur Edition";
            case "Brett":
                return "Femboy Edition";
            case "days":
                return "Junkie Edition";
            case "fernan":
                return "Beaner Edition";
            case "howl":
                return "Howl's Edition";
            case "Moonaw":
                return "Special Ed Edition";
            default:
                return null;
        }
    }
}
