package com.adjustclient.utilities.anticheat.player;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.render.hud.playerlist.TeamColors;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.utilities.anticheat.player.relation.RelationCache;
import com.google.common.collect.ComparisonChain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;

public final class TabPlayer implements Comparable<TabPlayer> {
    public final NetworkPlayerInfo info;
    public Object relationData;
    public PlayerEntry entry;
    public final boolean realPlayer;
    public Object field20;
    public TabPlayer(NetworkPlayerInfo info, boolean realPlayer) {
        this.info = info;
        this.realPlayer = realPlayer;
    }
    @Override public int compareTo(TabPlayer other) {
        if (Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS) {
            int left = TeamColors.getTeamIndex(info);
            int right = TeamColors.getTeamIndex(other.info);
            if (left != right) return left < right ? -1 : 1;
        }
        ScorePlayerTeam leftTeam = info.getPlayerTeam();
        ScorePlayerTeam rightTeam = other.info.getPlayerTeam();
        return ComparisonChain.start()
                .compareTrueFirst(info.getGameType() != WorldSettings.GameType.SPECTATOR,
                        other.info.getGameType() != WorldSettings.GameType.SPECTATOR)
                .compare(leftTeam == null ? "" : leftTeam.getRegisteredName(),
                        rightTeam == null ? "" : rightTeam.getRegisteredName())
                .compare(info.getGameProfile().getName(), other.info.getGameProfile().getName()).result();
    }
    public boolean isSelf() {
        return info.getGameProfile().equals(Minecraft.getMinecraft().thePlayer.getGameProfile());
    }
}
