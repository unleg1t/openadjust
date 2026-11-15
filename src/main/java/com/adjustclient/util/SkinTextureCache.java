package com.adjustclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import optifine.Config;

public class SkinTextureCache {
    public TextureManager textureManager;

    public SkinTextureCache(TextureManager textureManager) { this.textureManager = textureManager; }

    public void clean() {
        int before = textureManager.mapTextureObjects.size();
        List<ResourceLocation> stale = findStale(textureManager.mapTextureObjects);
        preservePlayerTextures(stale);
        for (ResourceLocation location : stale) {
            ITextureObject texture = textureManager.mapTextureObjects.remove(location);
            if (texture != null) {
                int textureId = texture.getGlTextureId();
                if (textureId != 0) net.minecraft.client.renderer.GlStateManager.deleteTexture(textureId);
            }
        }
        int after = textureManager.mapTextureObjects.size();
        if (Logger.debug) {
            Logger.log(LogLevel.DEBUG, true, "Garbage cleaned ({}) {} textures. Texture count was {}, now {}",
                    "reset", stale.size(), before, after);
        }
    }

    public void preservePlayerTextures(List<ResourceLocation> textures) {
        AbstractClientPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (Config.isShowCapes()) {
            ResourceLocation cape = player.getLocationCape();
            if (cape != null) textures.remove(cape);
        }
        ResourceLocation skin = player.getLocationSkin();
        if (skin != null) textures.remove(skin);
    }

    public List<ResourceLocation> findStale(Map<ResourceLocation, ITextureObject> textures) {
        List<ResourceLocation> stale = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ITextureObject> entry : textures.entrySet()) {
            ITextureObject texture = entry.getValue();
            if (texture instanceof ThreadDownloadImageData) {
                Timer lastUse = ((ThreadDownloadImageData) texture).lastUseTimer;
                if (lastUse.hasElapsed(4500L)) stale.add(entry.getKey());
            }
        }
        return stale;
    }
}
