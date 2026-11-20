package com.adjustclient.modules.impl.render.hud.playerlist.column;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.font.Fonts;

import java.awt.Color;
import java.util.List;

public final class GroupColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "Group";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        String text = entry.group;
        if (text == null) {
            return 0.0F;
        }
        return Fonts.TAHOMA_10.getStringWidth(text) + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        String text = entry.group;
        if (text != null) {
            Fonts.TAHOMA_10.drawString(text, x + 2.5F, y, new Color(150, 150, 150, 240).getRGB());
        }
    }
}
