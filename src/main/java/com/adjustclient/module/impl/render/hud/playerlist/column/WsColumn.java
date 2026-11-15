package com.adjustclient.module.impl.render.hud.playerlist.column;

import com.adjustclient.module.impl.render.hud.playerlist.StatValue;
import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.render.font.Fonts;

import java.util.List;

public final class WsColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "WS";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        StatValue<?> stat = entry.ws;
        if (stat == null) {
            return 0.0F;
        }
        return Fonts.TAHOMA_10.getStringWidth(stat.text) + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        StatValue<?> stat = entry.ws;
        if (stat != null) {
            Fonts.TAHOMA_10.drawString(stat.text, x + 2.5F, y, stat.color);
        }
    }
}
