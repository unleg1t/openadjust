package com.adjustclient.modules.impl.player.autorewound;

import java.util.regex.Pattern;

public final class PitChatPatterns {
    public static final Pattern ASSIST = Pattern.compile("^ASSIST! \\d+% on ");
    public static final Pattern KILL = Pattern.compile("^[A-Z ]*KILL! on ");
    public static final Pattern STREAK = Pattern.compile("^STREAK! of \\d+ kills by ");
    public static final Pattern BOUNTY = Pattern.compile("^BOUNTY! (of|bump) ");
    public static final Pattern BOUNTY_CLAIMED = Pattern.compile("^BOUNTY CLAIMED! ");
    public static final Pattern NIGHT_QUEST = Pattern.compile("^NIGHT QUEST ");
    public static final Pattern MEGASTREAK = Pattern.compile("^MEGASTREAK! ");
    public static final Pattern RNGESUS = Pattern.compile("^Apostle to RNGesus: ");

    private PitChatPatterns() {
    }
}
