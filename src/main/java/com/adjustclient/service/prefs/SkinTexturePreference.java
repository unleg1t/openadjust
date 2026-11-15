package com.adjustclient.service.prefs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class SkinTexturePreference extends SyncedPreference<StringPreferenceCodec> {
    public void update(GameProfile profile, MinecraftProfileTexture texture) {
        Session session = Minecraft.getMinecraft().getSession();
        if (session == null || profile == null || profile.getId() == null) return;
        if (!java.util.Objects.equals(session.getUsername(), profile.getName())) return;
        java.util.UUID sessionId = null;
        if (session.getPlayerID() != null && !session.getPlayerID().isEmpty()) {
            try {
                sessionId = UUIDTypeAdapter.fromString(session.getPlayerID());
            } catch (IllegalArgumentException ignored) {
                // Offline/dev launch without --uuid passes non-UUID playerID such as "Player0".
            }
        }
        if (sessionId == null && session.getUsername() != null) {
            sessionId = net.minecraft.entity.player.EntityPlayer.getOfflineUUID(session.getUsername());
        }
        if (profile.getId().equals(sessionId)) {
            setTexture(texture.getUrl(), texture.getHash());
        }
    }

    public void setTexture(String url, String hash) {
        if (isInvalidTexture(url, hash)) return;
        codec.value = hash;
    }

    public boolean isInvalidTexture(String url, String hash) {
        int scheme = url.indexOf("://");
        if (scheme < 3 || scheme >= 8) return true;
        if (hash.length() < 55 || hash.length() >= 75) return true;
        if (url.length() < 65) return true;
        if (!url.substring(scheme + 3).equals("textures.minecraft.net/texture/" + hash)) return true;
        return hash.matches("[0-9A-F]+");
    }
}
