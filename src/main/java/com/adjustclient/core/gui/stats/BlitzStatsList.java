package com.adjustclient.core.gui.stats;

import com.adjustclient.utilities.hypixel.api.model.player.stats.BlitzStats;
import com.adjustclient.utilities.RandomUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

final class BlitzStatsList extends StatsList {
    private final HypixelStatsScreen owner;

    BlitzStatsList(HypixelStatsScreen screen) { super(screen); this.owner = screen; }

    @Override public void populate() {
        for (net.minecraft.client.gui.GuiButton button : owner.buttons) {
            if (button == owner.blitzButton) button.enabled = false;
            else button.enabled = !owner.isUnavailable(button);
        }
        BlitzStats stats = owner.player.stats.hungerGames;
        if (stats == null) return;
        owner.rows.add(new ImmutablePair<>("Coins", String.format("%,d", stats.coins)));
        owner.rows.add(new ImmutablePair<>("Wins", String.format("%,d", stats.wins)));
        owner.rows.add(new ImmutablePair<>("Kills", String.format("%,d", stats.kills)));
        owner.rows.add(new ImmutablePair<>("Deaths", String.format("%,d", stats.deaths)));
        owner.rows.add(new ImmutablePair<>("K/D", Float.toString(RandomUtil.round((float) stats.kills / stats.deaths, 2))));
    }

    @Override public String getName() { return "Blitz SG"; }
}
