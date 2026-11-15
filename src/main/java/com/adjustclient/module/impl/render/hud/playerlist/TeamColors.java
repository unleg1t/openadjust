package com.adjustclient.module.impl.render.hud.playerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import com.adjustclient.module.impl.render.hud.playerlist.stats.BedWarsTeam;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

public final class TeamColors {

    private TeamColors() {
    }

    public static int getTeamIndex(NetworkPlayerInfo info) {
        java.util.Objects.requireNonNull(info);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return 8;
        }
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return 8;
        }
        ScorePlayerTeam team = scoreboard.getPlayersTeam(info.getGameProfile().getName());
        if (team == null) {
            return 8;
        }
        BedWarsTeam bedWarsTeam = BedWarsTeam.byTeamPrefix(team);
        if (bedWarsTeam == null) {
            return 8;
        }
        switch (TeamColorSwitchMap.COLOR[bedWarsTeam.chatColor.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
            case 8:
                return 7;
            default:
                return 8;
        }
    }
}
