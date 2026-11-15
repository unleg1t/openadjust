package com.adjustclient.util.anticheat.player.relation;

import com.adjustclient.util.anticheat.player.TabPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class TeamRelationResolver extends RelationResolver {
    public Object field4;
    @Override public PlayerRelation resolve(RelationCache player, ScorePlayerTeam team) {
        return team.getRegisteredName().startsWith("collideRule_-")
                ? PlayerRelation.VALID_PLAYER : PlayerRelation.NO_ACTION;
    }
}
