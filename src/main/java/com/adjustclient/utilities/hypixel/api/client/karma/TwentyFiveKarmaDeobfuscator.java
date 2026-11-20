package com.adjustclient.utilities.hypixel.api.client.karma;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

final class TwentyFiveKarmaDeobfuscator {
    private static final SecureRandom RANDOM;
    private static final DateTimeFormatter FORMAT;
    private static final BigInteger[] FACTORS;

    static {
        RANDOM = new SecureRandom();
        FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
        FACTORS = new BigInteger[]{
            new BigInteger("b5d35d9f5f", 16), new BigInteger("4ac367892d", 16),
            new BigInteger("8b55b16cad", 16), new BigInteger("5e6135c12d", 16),
            new BigInteger("195d15955b", 16), new BigInteger("a5188aa68d", 16),
            new BigInteger("6d1dae6727", 16), new BigInteger("7975262149", 16),
            new BigInteger("de38a40a87", 16), new BigInteger("74dcc5dbb3", 16),
            new BigInteger("5a53b7159f", 16), new BigInteger("cb2d1f710b", 16)
        };
    }

    public static String encodeNonce(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        byte[] key = new byte[]{107, 52, 114, 109, 52};
        for (int i = 0; i < bytes.length; i++) bytes[i] ^= key[i % key.length];
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String range(Instant time) {
        String hash = decimalHash(FORMAT.format(time.truncatedTo(ChronoUnit.MINUTES)));
        String start = hash.substring(hash.length() - 3, hash.length() - 2);
        String end = hash.substring(hash.length() - 2);
        if (end.charAt(0) == '0') end = "1" + end.substring(1);
        return "bytes " + start + "-" + end + "/*";
    }

    public static JsonElement decodeElement(JsonElement element, String key, int depth) {
        if (element == null) return null;
        if (element.isJsonObject()) return decodeObject(element.getAsJsonObject(), key, depth);
        if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement value : element.getAsJsonArray()) array.add(decodeElement(value, key, depth + 1));
            return array;
        }
        return element;
    }

    public static JsonObject decodeObject(JsonObject object, String key, int depth) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getKey().startsWith("__x_")) continue;
            result.add(decodeName(entry.getKey(), key, depth), decodeElement(entry.getValue(), key, depth + 1));
        }
        return result;
    }

    public static String decodeName(String name, String key, int depth) {
        StringBuilder base64 = new StringBuilder(name.replace('-', '+').replace('_', '/'));
        while ((base64.length() & 3) != 0) base64.append('=');
        byte[] bytes = Base64.getDecoder().decode(base64.toString());
        byte[] seed = (key + ":" + depth).getBytes(StandardCharsets.UTF_8);
        int state = 0x811c9dc5;
        for (byte b : seed) state = (state ^ (b & 255)) * 16777619;
        if (state == 0) state = 1;
        byte[] stream = new byte[bytes.length];
        for (int i = 0; i < stream.length; i++) {
            state ^= state << 13;
            state ^= state >>> 17;
            state ^= state << 5;
            stream[i] = (byte) state;
        }
        for (int i = 0; i < bytes.length; i++) bytes[i] ^= stream[i % stream.length];
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String responseKey(String method, String path, long epoch) {
        String hash = decimalHash(FORMAT.format(Instant.ofEpochSecond(60 * epoch).truncatedTo(ChronoUnit.MINUTES)));
        String start = hash.substring(hash.length() - 3, hash.length() - 2);
        String end = hash.substring(hash.length() - 2);
        if (end.charAt(0) == '0') end = "1" + end.substring(1);
        return "bytes " + start + "-" + end + "/*:25karma_player_obf:" + method + ":" + path;
    }

    public static String decimalHash(String value) {
        BigInteger result = FACTORS[10];
        for (int i = 0; i < value.length(); i++) {
            result = result.mod(FACTORS[11]).multiply(FACTORS[value.charAt(i) % 10]);
        }
        return result.toString();
    }

    public static String randomNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        StringBuilder value = new StringBuilder(32);
        for (byte b : bytes) {
            value.append(Character.forDigit((b >> 4) & 15, 16));
            value.append(Character.forDigit(b & 15, 16));
        }
        return value.toString();
    }
}
