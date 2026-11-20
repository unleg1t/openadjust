package com.adjustclient.modules.impl.render.hud.playerlist;

import com.adjustclient.utilities.game.rewound.RewoundGame;
import com.adjustclient.utilities.hypixel.HypixelGame;

final class PlayerListSwitchMap {

    static final int[] GAME = new int[HypixelGame.values().length];
    static final int[] REWOUND_GAME = new int[RewoundGame.values().length];

    static {
        GAME[HypixelGame.SKYWARS.ordinal()] = 1;
        GAME[HypixelGame.DUELS.ordinal()] = 2;
        GAME[HypixelGame.BED_WARS.ordinal()] = 3;
        GAME[HypixelGame.BLITZ_SG.ordinal()] = 4;
        GAME[HypixelGame.UHC_CHAMPIONS.ordinal()] = 5;
        GAME[HypixelGame.THE_HYPIXEL_PIT.ordinal()] = 6;
        GAME[HypixelGame.HOUSING.ordinal()] = 7;
        GAME[HypixelGame.SHEEP_WARS.ordinal()] = 8;
        GAME[HypixelGame.WOOL_WARS.ordinal()] = 9;
        GAME[HypixelGame.CAPTURE_THE_WOOL.ordinal()] = 10;
        GAME[HypixelGame.ATLAS.ordinal()] = 11;
        GAME[HypixelGame.REPLAY.ordinal()] = 12;
        GAME[HypixelGame.MURDER_MYSTERY.ordinal()] = 13;
        GAME[HypixelGame.BUILD_BATTLE.ordinal()] = 14;
        GAME[HypixelGame.BED_WARS_PRACTICE.ordinal()] = 15;
        GAME[HypixelGame.HALLOWEEN_SIMULATOR.ordinal()] = 16;
        GAME[HypixelGame.GRINCH_SIMULATOR.ordinal()] = 17;
        GAME[HypixelGame.EASTER_SIMULATOR.ordinal()] = 18;
        GAME[HypixelGame.TNT_TAG.ordinal()] = 19;

        REWOUND_GAME[RewoundGame.SKYWARS.ordinal()] = 1;
        REWOUND_GAME[RewoundGame.DUELS.ordinal()] = 2;
        REWOUND_GAME[RewoundGame.BEDWARS.ordinal()] = 3;
        REWOUND_GAME[RewoundGame.SURVIVAL_GAMES.ordinal()] = 4;
        REWOUND_GAME[RewoundGame.PIT.ordinal()] = 5;
        REWOUND_GAME[RewoundGame.REPLAY.ordinal()] = 6;
    }
}
