package com.adjustclient.module.impl.render.hud.eventtimer;

import com.adjustclient.hypixel.HypixelGame;

final class EventTimersSwitchMap {

    static final int[] GAME = new int[HypixelGame.values().length];

    static {
        GAME[HypixelGame.THE_HYPIXEL_PIT.ordinal()] = 1;
        GAME[HypixelGame.DUELS.ordinal()] = 2;
        GAME[HypixelGame.SKYWARS.ordinal()] = 3;
        GAME[HypixelGame.BED_WARS.ordinal()] = 4;
    }
}
