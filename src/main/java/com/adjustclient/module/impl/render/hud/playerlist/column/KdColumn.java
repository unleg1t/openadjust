package com.adjustclient.module.impl.render.hud.playerlist.column;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.render.font.Fonts;

import java.awt.Color;
import java.util.List;

public final class KdColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "K/D";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        String text = entry.kd;
        if (text == null) {
            return 0.0F;
        }
        return Fonts.TAHOMA_10.getStringWidth(text) + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        String text = entry.kd;
        if (text != null) {
            Fonts.TAHOMA_10.drawString(text, x + 2.5F, y, new Color(150, 150, 150, 240).getRGB());
        }
    }
}
