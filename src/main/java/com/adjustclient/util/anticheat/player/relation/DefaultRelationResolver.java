package com.adjustclient.util.anticheat.player.relation;

import com.adjustclient.util.anticheat.player.TabPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class DefaultRelationResolver extends RelationResolver {
    public Object field4;
    public final long delay;
    public DefaultRelationResolver(long delay) { this.delay = delay; }
    @Override public PlayerRelation resolve(RelationCache player, ScorePlayerTeam team) {
        return PlayerRelation.NO_ACTION;
    }
}
