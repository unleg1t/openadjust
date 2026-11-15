package com.adjustclient.util.anticheat.player.relation;

import com.adjustclient.util.anticheat.player.TabPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public abstract class RelationResolver {
    public abstract PlayerRelation resolve(RelationCache player, ScorePlayerTeam team);
}
