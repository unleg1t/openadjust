package com.adjustclient.service.telemetry;

public enum BypassFeature {
    NOFALL_SPOOF_NOGROUND(""),
    NOFALL_SPOOF(""),
    NOFALL_WATER(""),
    NOFALL_HYPIXEL(""),
    NOFALL_PACKET(""),
    NOSLOWDOWN_HYPIXEL_SWORD(""),
    NOSLOWDOWN_HYPIXEL_CONSUMABLES(""),
    SPEED_HYPIXEL_STRAFE_GLIDE(""),
    SPEED_HYPIXEL_STRAFE_RANGE(""),
    GAME_SPEED_NORMAL(""),
    ANTIAIM_HEADLESS(""),
    HALLOWEEN_SIMULATOR(""),
    LONGJUMP_HYPIXEL_BOW(""),
    LONGJUMP_HYPIXEL_FIREBALL(""),
    LONGJUMP_HYPIXEL_REGAIN_HBUF(""),
    BREAKER_BREAK_AGAINST_BLOCK(""),
    BREAKER_BREAK(""),
    SPRINT_OMNI_PREDICTIVE(""),
    INVENTORY_MOVE_HYPIXEL(""),
    LAGBREAK(""),
    FREECAM_NUKER(""),
    FREECAM_INVISIBLE(""),
    SCAFFOLD_HYPIXEL_TOWER(""),
    SCAFFOLD_JUMP(""),
    SCAFFOLD_NO_ASCEND(""),
    SCAFFOLD_FLAT(""),
    SCAFFOLD_SNEAK(""),
    ROTATE_OMNI_SPRINT("");

    public final String detail;
    public final boolean allowFreecam;

    BypassFeature(String detail) {
        this.detail = detail;
        this.allowFreecam = name().contains("FREECAM");
    }
}
