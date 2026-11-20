package com.adjustclient.core.gui.stats;

import com.adjustclient.utilities.hypixel.api.model.player.stats.UhcStats;
import com.adjustclient.utilities.RandomUtil;
import net.minecraft.client.gui.GuiButton;
import org.apache.commons.lang3.tuple.ImmutablePair;

final class UhcStatsList extends StatsList {
    private final HypixelStatsScreen owner;

    UhcStatsList(HypixelStatsScreen screen) { super(screen); owner = screen; }

    @Override public void populate() {
        for (GuiButton button : owner.buttons) {
            if (button == owner.uhcButton) button.enabled = false;
            else button.enabled = !owner.isUnavailable(button);
        }
        UhcStats stats = owner.player.stats.uhc;
        if (stats == null) return;
        owner.rows.add(new ImmutablePair<>("Coins", String.format("%,d", stats.coins)));
        owner.rows.add(new ImmutablePair<>("Wins", String.format("%,d", stats.wins)));
        owner.rows.add(new ImmutablePair<>("Kills", String.format("%,d", stats.kills)));
        owner.rows.add(new ImmutablePair<>("Deaths", String.format("%,d", stats.deaths)));
        owner.rows.add(new ImmutablePair<>("K/D", Float.toString(RandomUtil.round((float) stats.kills / stats.deaths, 2))));
    }

    @Override public String getName() { return "UHC"; }
}
