package com.adjustclient.modules.impl.render.hud.playerlist.menu;

import com.adjustclient.utilities.hypixel.HypixelGame;

final class MenuGameSwitchMap {

    static final int[] GAME = new int[HypixelGame.values().length];

    static {
        GAME[HypixelGame.THE_HYPIXEL_PIT.ordinal()] = 1;
        GAME[HypixelGame.SKYBLOCK.ordinal()] = 2;
    }
}
