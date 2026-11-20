package com.adjustclient.utilities.lunar.apollo;

import com.adjustclient.utilities.lunar.LunarClientService;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoValue;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoValueKind;
import com.adjustclient.utilities.lunar.apollo.protocol.player.PlayerHandshakeMessage;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

final class LunarModStatusBuilder {
    public static String randomHitboxColor(Random random) {
        int index = random.nextInt(6);
        return new String[]{"ff000000", "ff000000", "ff000000", "ff333333", "ffff0000", "ff00ff00"}[index];
    }

    public static LinkedHashMap<String, ProtoValue> buildStatus(UUID player) {
        Random random = new Random(playerSeed(LunarClientService.INSTANCE.seed, player));
        LinkedHashMap<String, ProtoValue> status = new LinkedHashMap<>();
        maybeBoolean(status, random, "cooldowns.enabled", false, 0.3);
        maybeBoolean(status, random, "direction-hud.enabled", false, 0.35);
        maybeBoolean(status, random, "horse-stats.enabled", false, 0.4);
        maybeBoolean(status, random, "markers.enabled", false, 0.35);
        maybeBoolean(status, random, "waypoints.enabled", false, 0.3);
        maybeBoolean(status, random, "radio.enabled", false, 0.5);
        maybeBoolean(status, random, "rewind.enabled", false, 0.4);
        maybeBoolean(status, random, "menu-blur.enabled", false, 0.35);
        maybeBoolean(status, random, "team-view.enabled", false, 0.4);
        maybeBoolean(status, random, "mumble-link.enabled", false, 0.25);
        maybeBoolean(status, random, "audio-subtitles.enabled", false, 0.2);
        maybeBoolean(status, random, "hitbox.enabled", true, 0.55);
        maybeBoolean(status, random, "crosshair.enabled", true, 0.5);
        maybeBoolean(status, random, "hit-color.enabled", true, 0.45);
        maybeBoolean(status, random, "particle-changer.enabled", true, 0.4);
        maybeBoolean(status, random, "overlay-mod.enabled", true, 0.35);
        maybeBoolean(status, random, "hurt-cam.enabled", true, 0.4);
        maybeBoolean(status, random, "block-outline.enabled", true, 0.35);
        maybeBoolean(status, random, "nick-hider.hide-lobby-i-d", true, 0.3);
        maybeBoolean(status, random, "scoreboard.text-shadow", true, 0.45);
        maybeBoolean(status, random, "scoreboard.numbers", true, 0.35);
        maybeBoolean(status, random, "nametag.nametag-shadow", true, 0.4);
        maybeBoolean(status, random, "chat.chat-name-bold", true, 0.3);
        maybeBoolean(status, random, "chat.modern-chat-length-hypixel", true, 0.35);
        maybeBoolean(status, random, "chat.chat-height", true, 0.25);
        maybeBoolean(status, random, "tab.hide-n-p-c", true, 0.35);
        maybeBoolean(status, random, "zoom.variable-zoom", true, 0.4);
        maybeBoolean(status, random, "zoom.smooth-zoom", false, 0.3);
        maybeBoolean(status, random, "zoom.smooth-camera", false, 0.35);
        maybeBoolean(status, random, "freelook.smooth-camera", false, 0.3);
        maybeBoolean(status, random, "hurt-cam.disable-hurt-cam", true, 0.35);
        maybeBoolean(status, random, "nick-hider.hide-real-name", false, 0.25);
        maybeBoolean(status, random, "bossbar.render-bar", false, 0.3);
        maybeBoolean(status, random, "bossbar.use-minecraft-g-u-i-scale", false, 0.2);
        maybeBoolean(status, random, "coordinates.background", false, 0.25);
        maybeBoolean(status, random, "potion-effects.background", false, 0.3);
        maybeBoolean(status, random, "overlay-mod.custom-fishing-line", true, 0.25);
        maybeBoolean(status, random, "block-outline.block-overlay", true, 0.3);
        maybeBoolean(status, random, "particle-changer.always-enchant-strikes", true, 0.3);
        if (status.containsKey("hitbox.enabled")) {
            String color = randomHitboxColor(random);
            String[] targets = {"hitbox-player-line-color", "hitbox-monster-line-color", "hitbox-passive-line-color",
                    "hitbox-projectile-line-color", "hitbox-arrow-line-color", "hitbox-item-line-color",
                    "hitbox-snowball-line-color", "hitbox-fireball-line-color", "hitbox-wither-skull-line-color",
                    "hitbox-firework-line-color", "hitbox-exp-orb-line-color", "hitbox-item-frame-line-color",
                    "hitbox-other-line-color"};
            for (String target : targets) {
                ProtoValue setting = new ProtoValue(ProtoValueKind.STRING_VALUE, color);
                status.put("hitbox." + target, setting);
            }
        }
        maybeNumber(status, random, "nametag.nametag-background-opacity", 0.25, new double[]{0, 0, 0.15, 0.25});
        maybeNumber(status, random, "chat.chat-background-opacity", 0.2, new double[]{0, 0, 0.1});
        maybeNumber(status, random, "block-outline.block-outline-width", 0.25, new double[]{3, 3, 4, 5});
        maybeNumber(status, random, "overlay-mod.fishing-line-thickness", 0.2, new double[]{2, 2, 3});
        maybeString(status, random, "hit-color.hit-armor-color", 0.3, new String[]{"91000000", "91000000", "64ff0000", "91333333"});
        maybeString(status, random, "block-outline.block-outline-color", 0.25, new String[]{"a0000000", "ff000000", "a0333333"});
        maybeString(status, random, "block-outline.block-overlay-color", 0.2, new String[]{"46000000", "20000000", "33000000"});
        return status;
    }

    public static void populateModStatus(PlayerHandshakeMessage handshake, UUID player) {
        for (Map.Entry<String, ProtoValue> entry : buildStatus(player).entrySet())
            handshake.putModStatus(entry.getKey(), entry.getValue());
    }

    private static void maybeBoolean(Map<String, ProtoValue> status, Random random,
                                     String key, boolean value, double probability) {
        if (random.nextDouble() >= probability) return;
        ProtoValue setting = new ProtoValue(ProtoValueKind.BOOL_VALUE, Boolean.valueOf(value));
        status.put(key, setting);
    }

    private static void maybeNumber(Map<String, ProtoValue> status, Random random,
                                    String key, double probability, double[] values) {
        if (random.nextDouble() >= probability) return;
        double value = values[random.nextInt(values.length)];
        ProtoValue setting = new ProtoValue(ProtoValueKind.NUMBER_VALUE, Double.valueOf(value));
        status.put(key, setting);
    }

    private static void maybeString(Map<String, ProtoValue> status, Random random,
                                    String key, double probability, String[] values) {
        if (random.nextDouble() >= probability) return;
        String value = java.util.Objects.requireNonNull(values[random.nextInt(values.length)], "value");
        ProtoValue setting = new ProtoValue(ProtoValueKind.STRING_VALUE, value);
        status.put(key, setting);
    }

    public static long playerSeed(long installationSeed, UUID player) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] input = ByteBuffer.allocate(24).putLong(installationSeed)
                    .putLong(player.getMostSignificantBits()).putLong(player.getLeastSignificantBits()).array();
            return ByteBuffer.wrap(digest.digest(input)).getLong();
        } catch (NoSuchAlgorithmException error) {
            return installationSeed ^ player.getLeastSignificantBits();
        }
    }
}
