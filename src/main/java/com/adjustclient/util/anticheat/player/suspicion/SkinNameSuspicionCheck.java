package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class SkinNameSuspicionCheck extends SuspicionCheck {
    public Object field4;
    private static final List<NameCheck> CHECKS;
    private static final String[] COMMON_SKINS;
    static {
        CHECKS = new ArrayList<>();
        CHECKS.add(new NamePatternCheck());
        CHECKS.add(new AltShopNameCheck());
        CHECKS.add(new DefaultSkinCheck());
        COMMON_SKINS = new String[] {
            "http://textures.minecraft.net/texture/31f477eb1a7beee631c2ca64d06f8f68fa93a3386d04452ab27f43acdf1b60cb",
            "http://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f",
            "http://textures.minecraft.net/texture/4c05ab9e07b3505dc3ec11370c3bdce5570ad2fb2b562e9b9dd9cf271f81aa44",
            "http://textures.minecraft.net/texture/6ac6ca262d67bcfb3dbc924ba8215a18195497c780058a5749de674217721892",
            "http://textures.minecraft.net/texture/6c160fbd16adbc4bff2409e70180d911002aebcfa811eb6ec3d1040761aea6dd",
            "http://textures.minecraft.net/texture/90e75cd429ba6331cd210b9bd19399527ee3bab467b5a9f61cb8a27b177f6789",
            "http://textures.minecraft.net/texture/7cb3ba52ddd5cc82c0b050c3f920f87da36add80165846f479079663805433db",
            "http://textures.minecraft.net/texture/dc0fcfaf2aa040a83dc0de4e56058d1bbb2ea40157501f3e7d15dc245e493095",
            "http://textures.minecraft.net/texture/fece7017b1bb13926d1158864b283b8b930271f80a90482f174cca6a17e88236",
            "http://textures.minecraft.net/texture/daf3d88ccb38f11f74814e92053d92f7728ddb1a7955652a60e30cb27ae6659f",
            "http://textures.minecraft.net/texture/46acd06e8483b176e8ea39fc12fe105eb3a2a4970f5100057e9d84d4b60bdfa7",
            "http://textures.minecraft.net/texture/1abc803022d8300ab7578b189294cce39622d9a404cdc00d3feacfdf45be6981",
            "http://textures.minecraft.net/texture/e5cdc3243b2153ab28a159861be643a4fc1e3c17d291cdd3e57a7f370ad676f3",
            "http://textures.minecraft.net/texture/226c617fde5b1ba569aa08bd2cb6fd84c93337532a872b3eb7bf66bdd5b395f8",
            "http://textures.minecraft.net/texture/b66bc80f002b10371e2fa23de6f230dd5e2f3affc2e15786f65bc9be4c6eb71a",
            "http://textures.minecraft.net/texture/a3bd16079f764cd541e072e888fe43885e711f98658323db0f9a6045da91ee7a",
            "http://textures.minecraft.net/texture/eee522611005acf256dbd152e992c60c0bb7978cb0f3127807700e478ad97664",
            "http://textures.minecraft.net/texture/f5dddb41dcafef616e959c2817808e0be741c89ffbfed39134a13e75b811863d"
        };
    }
    public static boolean isCommonSkin(String skinUrl) {
        return Arrays.asList(COMMON_SKINS).contains(skinUrl);
    }
    @Override public double score(PlayerEntry player, double score) {
        GameProfile profile = player.player.info.getGameProfile();
        String name = profile.getName();
        if (!profile.getProperties().containsKey("textures")) return score;
        if (profile.getProperties().get("textures").isEmpty()) return score;
        String skinUrl = null;
        try {
            Property texture = (Property) profile.getProperties().get("textures").toArray()[0];
            String decoded = new String(Base64.getDecoder().decode(texture.getValue()));
            JsonElement parsed = new JsonParser().parse(decoded);
            // JsonParser enforces complete consumption ("Did not consume the entire document.").
            JsonObject textures = parsed.getAsJsonObject().getAsJsonObject("textures");
            if (textures != null) {
                JsonObject skin = textures.getAsJsonObject("SKIN");
                if (skin != null && skin.get("url") != null) skinUrl = skin.get("url").getAsString();
            }
        } catch (Exception malformedTexture) {
            skinUrl = null;
        }
        double maximum = score;
        for (NameCheck check : CHECKS) {
            double candidate = check.score(name, skinUrl, score);
            if (candidate > maximum) maximum = candidate;
        }
        return maximum;
    }
}
