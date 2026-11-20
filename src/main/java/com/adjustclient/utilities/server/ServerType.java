package com.adjustclient.utilities.server;

public enum ServerType {
    HYPIXEL("Hypixel", "hypixel.net", "textures/server/hypixel.png", 340705396),
    BLOCKSMC("BlocksMC", "blocksmc.com", "textures/server/blocksmc.png", 174108543),
    MINEMEN_CLUB("Minemen Club", "minemen.club", "textures/server/mmc.png", 329183918),
    BEDWARS_PRACTICE("Bedwars Practice", "bedwarspractice.club", "textures/server/bwp.png", 191298381),
    ROXBOT("ROXBOT", "roxbot.com", "textures/server/roxbot.png", 1150916305),
    _2B2T("2b2t", "2b2t.org", "", 215012044),
    MINEPLEX("Mineplex", "mineplex.com", "", 1053496398),
    ANTICHEAT_TEST("Anti-Cheat Test", "anticheat-test.com", "", 842858960),
    LOYISA("Loyisa", "eu.loyisa.cn", "", 1395715795),
    REWOUND("Rewound", "play.rewound.cc", "textures/server/rewound.png", 552218335);
    public final String displayName;
    public final String domain;
    public final String icon;
    public final int id;
    public static final java.util.List<ServerType> all = java.util.Arrays.asList(values());
    public static final java.util.Map<Integer, ServerType> byId = all.stream().collect(
        java.util.stream.Collectors.toMap(server -> server.id, java.util.function.Function.identity()));
    ServerType(String displayName, String domain, String icon, int id) {
        this.displayName = displayName; this.domain = domain; this.icon = icon; this.id = id;
    }
    @Override public String toString() { return "KnownServerTypes{name='" + displayName + "'}"; }
}
