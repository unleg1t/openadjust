package com.adjustclient.module.impl.render.hud.playerlist;

import com.adjustclient.hypixel.HypixelGame;

public final class StatsSwitchMap {

    public static final int[] PAIR = new int[2];

    public static final int[] GAME = new int[HypixelGame.values().length];

    static {
        PAIR[0] = 1;
        PAIR[1] = 2;
        GAME[HypixelGame.SKYWARS.ordinal()] = 1;
        GAME[HypixelGame.BED_WARS.ordinal()] = 2;
        GAME[HypixelGame.BED_WARS_PRACTICE.ordinal()] = 3;
        GAME[HypixelGame.WOOL_WARS.ordinal()] = 4;
        GAME[HypixelGame.SHEEP_WARS.ordinal()] = 5;
        GAME[HypixelGame.CAPTURE_THE_WOOL.ordinal()] = 6;
        GAME[HypixelGame.MURDER_MYSTERY.ordinal()] = 7;
    }
}
