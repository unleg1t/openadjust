package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;

public final class SuspicionScorer {
    private static final SuspicionCheck[] CHECKS;
    static {
        CHECKS = new SuspicionCheck[] { new HypixelStatsCheckB(), new HypixelStatsCheckC(),
                new AccountAgeCheck(), new SkinNameSuspicionCheck(), new LanguageCheck(),
                new HypixelRankCheck(), new HypixelStatsCheckE() };
    }
    public static double score(PlayerEntry player) {
        double score = 0.0;
        for (SuspicionCheck check : CHECKS) {
            if (check.applies()) score = check.score(player, score);
        }
        return score;
    }
}
