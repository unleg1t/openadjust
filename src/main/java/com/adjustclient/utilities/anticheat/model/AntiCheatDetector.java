package com.adjustclient.utilities.anticheat.model;

import com.adjustclient.core.events.ProtocolWarningType;
import com.adjustclient.utilities.server.ServerType;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatColor;

public final class AntiCheatDetector {
    private static final AntiCheatModelFactory<?>[] factories;

    private static final AntiCheatModelFactory<AntiCheatModel> roxbot = (address, port, data) -> detectRoxbot(address, port, data);
    private static final AntiCheatModelFactory<LiquidBounceTestModel> liquidBounce = (address, port, data) -> detectLiquidBounce(address);
    private static final AntiCheatModelFactory<AntiGamingChairModel> minemen = (address, port, data) -> detectMinemen(address, port, data);
    private static final AntiCheatModelFactory<AntiCheatModel> mineplex = (address, port, data) -> detectMineplex(address, port, data);
    private static final AntiCheatModelFactory<VanillaModel> antiCheatTest = (address, port, data) -> {
        if (!matchesDomain(address.toLowerCase(), "anticheat-test.com")) return null;
        VanillaModel model = new VanillaModel(ServerType.ANTICHEAT_TEST);
        model.field16.add(ProtocolWarningType.UNKNOWN_CUSTOM_PAYLOAD);
        return model;
    };
    private static final AntiCheatModelFactory<GrimModel> blocksMC = (address, port, data) -> detectBlocksMC(address, port, data);
    private static final AntiCheatModelFactory<WatchdogModel> hypixel = (address, port, data) -> detectHypixel(address, port, data);
    private static final AntiCheatModelFactory<VoxylModel> voxyl = (address, port, data) -> detectVoxyl(address, port, data);
    private static final AntiCheatModelFactory<CiyunaModel> rewound = (address, port, data) -> detectRewound(address);

    static {
        factories = new AntiCheatModelFactory<?>[] {
                hypixel, blocksMC, minemen, voxyl, roxbot, mineplex, rewound, antiCheatTest, liquidBounce
        };
    }

    public static boolean matchesDomain(String address, String domain) {
        if (address.contains(":")) address = address.split(":")[0];
        return address.equals(domain) || address.equals(domain + ".")
                || address.endsWith("." + domain) || address.endsWith("." + domain + ".");
    }

    public static AntiCheatModel detectModel(String address, int port, ServerData data) {
        for (AntiCheatModelFactory<?> factory : factories) {
            AntiCheatModel model = factory.identifyKnownServerBy(address, port, data);
            if (model != null) return model;
        }
        return null;
    }

    public static LiquidBounceTestModel detectLiquidBounce(String address) {
        String host = address.toLowerCase();
        return matchesDomain(host, "ccbluex.net") || matchesDomain(host, "loyisa.cn")
                ? new LiquidBounceTestModel(ServerType.LOYISA) : null;
    }

    public static AntiCheatModel detectMineplex(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(host, "liquidproxy.net")) {
            if (data.serverMOTD != null && ChatColor.getTextWithoutFormattingCodes(data.serverMOTD).trim().equals("[ Mineplex Games ]"))
                return new VulcanGrimModel(ServerType.ROXBOT);
            return null;
        }
        return matchesDomain(host, "mineplex.com") ? new PolarModel(ServerType.MINEPLEX) : null;
    }

    public static AntiCheatModel detectRoxbot(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(host, "liquidproxy.net")) {
            if (data.serverMOTD != null && ChatColor.getTextWithoutFormattingCodes(data.serverMOTD).trim().equals(">>ROXBOT.COM | Deathmatch Kit PVP >>discord.roxbot.com"))
                return new VulcanGrimModel(ServerType.ROXBOT);
            return null;
        }
        return matchesDomain(host, "roxbot.com") ? new VulcanGrimModel(ServerType.ROXBOT) : null;
    }

    public static VoxylModel detectVoxyl(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(host, "liquidproxy.net")) {
            if (data.serverMOTD != null && ChatColor.getTextWithoutFormattingCodes(data.serverMOTD).trim().contains("Bedwars Practice [1.8-"))
                return new VoxylModel(ServerType.BEDWARS_PRACTICE);
            return null;
        }
        return matchesDomain(host, "bedwarspractice.club") || matchesDomain(host, "voxyl.net")
                ? new VoxylModel(ServerType.BEDWARS_PRACTICE) : null;
    }

    public static AntiGamingChairModel detectMinemen(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(host, "liquidproxy.net")) {
            return data.serverMOTD != null && data.serverMOTD.trim().startsWith("§d§lMinemen Club")
                    ? new AntiGamingChairModel(ServerType.MINEMEN_CLUB) : null;
        }
        return matchesDomain(host, "minemen.club") || matchesDomain(host, "mineman.club")
                ? new AntiGamingChairModel(ServerType.MINEMEN_CLUB) : null;
    }

    public static GrimModel detectBlocksMC(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(host, "liquidproxy.net")) {
            return data.serverMOTD != null && ChatColor.getTextWithoutFormattingCodes(data.serverMOTD).contains("✴ BlocksMC ✴")
                    ? new GrimModel(ServerType.BLOCKSMC) : null;
        }
        return matchesDomain(host, "blocksmc.com") ? new GrimModel(ServerType.BLOCKSMC) : null;
    }

    public static CiyunaModel detectRewound(String address) {
        return matchesDomain(address.toLowerCase(), "rewound.cc") ? new CiyunaModel(ServerType.REWOUND) : null;
    }

    public static WatchdogModel detectHypixel(String address, int port, ServerData data) {
        String host = address.toLowerCase();
        if (matchesDomain(address, "liquidproxy.net")) {
            return data.gameVersion.matches("Requires MC 1\\.8 / 1\\.\\d{2}") ? new WatchdogModel(ServerType.HYPIXEL, false) : null;
        }
        if (!matchesDomain(host, "hypixel.net") && !matchesDomain(host, "hypixel.io")) return null;
        return new WatchdogModel(ServerType.HYPIXEL, !host.equalsIgnoreCase("alpha.hypixel.net"));
    }
}
