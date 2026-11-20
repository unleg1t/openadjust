package com.adjustclient.modules.impl.render.hud.playerlist.column;

import com.adjustclient.modules.impl.render.hud.playerlist.StatValue;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.font.Fonts;

import java.util.List;

public final class WlColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "W/L";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        StatValue<?> stat = entry.wl;
        if (stat == null) {
            return 0.0F;
        }
        return Fonts.TAHOMA_10.getStringWidth(stat.text) + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        StatValue<?> stat = entry.wl;
        if (stat != null) {
            Fonts.TAHOMA_10.drawString(stat.text, x + 2.5F, y, stat.color);
        }
    }
}
