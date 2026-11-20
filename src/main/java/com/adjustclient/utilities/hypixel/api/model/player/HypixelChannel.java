package com.adjustclient.utilities.hypixel.api.model.player;

public enum HypixelChannel {
    ALL("All"),
        PM("PM"),
        PARTY("Party"),
        GUILD("Guild"),
        OFFICER("Officer"),
        SKYBLOCK_COOP("SkyBlock Co-op"),
        STAFF("Staff");

    public final String displayName;

    HypixelChannel(String displayName) { this.displayName = displayName; }
}
