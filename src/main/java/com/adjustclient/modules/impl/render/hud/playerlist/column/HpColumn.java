package com.adjustclient.modules.impl.render.hud.playerlist.column;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.font.Fonts;

import java.awt.Color;
import java.util.List;

public final class HpColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "HP";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        String text = entry.healthText;
        if (text == null) {
            return 0.0F;
        }
        return Fonts.TAHOMA_10.getStringWidth(text) + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        String text = entry.healthText;
        if (text != null) {
            int alpha = entry.noEntity ? 130 : 240;
            Fonts.TAHOMA_10.drawString(text, x + 2.5F, y, new Color(170, 170, 170, alpha).getRGB());
        }
    }
}
