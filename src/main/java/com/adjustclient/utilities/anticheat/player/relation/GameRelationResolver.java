package com.adjustclient.utilities.anticheat.player.relation;

import com.adjustclient.utilities.anticheat.player.TabPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import net.minecraft.scoreboard.Team;

public final class GameRelationResolver extends RelationResolver {
    public Object field4;
    @Override public PlayerRelation resolve(RelationCache player, ScorePlayerTeam team) {
        if (team.nameTagVisibility == Team.EnumVisible.NEVER) return PlayerRelation.NO_ACTION;
        return player.info.getResponseTime() != 0 || Adjust.gameStateTracker.state.game == HypixelGame.DUELS
                ? PlayerRelation.VALID_PLAYER : PlayerRelation.NO_ACTION;
    }
}
