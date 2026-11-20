package com.adjustclient.core.gui.stats;

import com.adjustclient.utilities.hypixel.api.model.player.stats.SkyWarsStats;
import com.adjustclient.utilities.RandomUtil;
import net.minecraft.client.gui.GuiButton;
import org.apache.commons.lang3.tuple.ImmutablePair;

final class SkyWarsStatsList extends StatsList {
    private final HypixelStatsScreen owner;

    SkyWarsStatsList(HypixelStatsScreen screen) { super(screen); owner = screen; }

    @Override public void populate() {
        for (GuiButton button : owner.buttons) {
            if (button == owner.skyWarsButton) button.enabled = false;
            else button.enabled = !owner.isUnavailable(button);
        }
        SkyWarsStats stats = owner.player.stats.skyWars;
        if (stats == null) return;
        Integer level = stats.getLevel();
        owner.rows.add(new ImmutablePair<>("Game Level", String.format("%,d", level == null ? 0 : level)));
        owner.rows.add(new ImmutablePair<>("Coins", String.format("%,d", stats.coins)));
        if (stats.winstreak != null) owner.rows.add(new ImmutablePair<>("Win Streak", String.format("%,d", stats.winstreak)));
        else if (stats.wins != null) owner.rows.add(new ImmutablePair<>("Win Streak", "API off"));
        owner.rows.add(new ImmutablePair<>("Games Played", String.format("%,d", stats.gamesPlayed)));
        int wins = stats.wins == null ? 0 : stats.wins;
        owner.rows.add(new ImmutablePair<>("Wins", String.format("%,d", wins)));
        int losses = stats.losses == null ? 0 : stats.losses;
        owner.rows.add(new ImmutablePair<>("Losses", String.format("%,d", losses)));
        owner.rows.add(new ImmutablePair<>("W/L", Float.toString(RandomUtil.round((float) wins / losses, 2))));
        owner.rows.add(new ImmutablePair<>("Kills", String.format("%,d", stats.kills)));
        owner.rows.add(new ImmutablePair<>("Deaths", String.format("%,d", stats.deaths)));
        owner.rows.add(new ImmutablePair<>("K/D", Float.toString(RandomUtil.round((float) stats.kills / stats.deaths, 2))));
    }

    @Override public String getName() { return "Sky Wars"; }
}
