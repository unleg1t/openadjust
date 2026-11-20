package com.adjustclient.utilities.anticheat.player.relation;

import com.adjustclient.utilities.anticheat.player.TabPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public abstract class RelationResolver {
    public abstract PlayerRelation resolve(RelationCache player, ScorePlayerTeam team);
}
