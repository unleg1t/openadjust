package com.adjustclient.modules.impl.render.hud;

final class HudSwitchMap {

    static final int[] MODE = new int[2];

    static {
        MODE[0] = 2;     // *(int*)(array + 8) = 2   (BindMode.PRESSED)
        MODE[1] = 1;     // *(int*)(array + 12) = 1  (BindMode.RELEASED)
    }
}
