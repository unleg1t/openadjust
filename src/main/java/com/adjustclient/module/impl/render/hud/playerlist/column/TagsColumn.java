package com.adjustclient.module.impl.render.hud.playerlist.column;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;

import java.util.List;
import com.adjustclient.hypixel.api.model.cubelify.CubelifyTag;
import com.adjustclient.render.font.svg.FitMode;
import com.adjustclient.render.font.svg.MdiIconCache;
import com.adjustclient.render.font.svg.SvgImage;

public final class TagsColumn extends PlayerListColumn {

    @Override
    public String getName() {
        return "Tags";
    }

    @Override
    public float getWidth(PlayerEntry entry) {
        CubelifyTag[] tags = entry.tags;
        int count = tags.length;
        if (count == 0) {
            return 0.0F;
        }
        return count * 10.0F + 5.5F;
    }

    @Override
    public void render(List<Runnable> deferred, PlayerEntry entry, float x, float y) {
        CubelifyTag[] tags = entry.tags;
        float cx = x + 2.5F;
        for (CubelifyTag tag : tags) {
            SvgImage icon = MdiIconCache.INSTANCE.getIcon(tag.icon);
            if (icon == null) {
                cx += 10.0F;
                continue;
            }
            int color = tag.color | 0xF0000000;                  // tag colour with alpha 240
            int shadow = (color & 0xFF000000) | ((tag.color & 0xFCFCFC) >> 2);
            icon.draw(FitMode.CONTAIN, shadow, cx + 1.5F, y - 1.5F, 6.0F, 6.0F, 1.0F);
            icon.draw(FitMode.CONTAIN, color, cx + 1.0F, y - 2.0F, 6.0F, 6.0F, 1.0F);
            cx += 10.0F;
        }
    }
}
