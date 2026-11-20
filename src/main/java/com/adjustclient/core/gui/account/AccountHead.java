package com.adjustclient.core.gui.account;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.GL11;

import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.utilities.Timer;
import com.adjustclient.Adjust;

public final class AccountHead {

    public ResourceLocation skin;
    public GameProfile profile;
    public String name;
    public String uuid;
    public MinecraftProfileTexture texture;
    Timer drawTimer;
    public UUID parsedUuid;
    public boolean requested;
    public boolean loaded;
    Object obf36;

    public AccountHead(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.drawTimer = new Timer();
    }

    /** True when {@code other} (dashes removed) equals this head's UUID (dashes removed), ignoring case. */
    public boolean matchesUuid(String other) {
        if (other == null || this.uuid == null) return java.util.Objects.equals(other, this.uuid);
        return other.replace("-", "").equalsIgnoreCase(this.uuid.replace("-", ""));
    }

    /** True when this head belongs to the given (name, uuid): same name and {@link #matchesUuid}. */
    public boolean isFor(String name, String uuid) {
        return java.util.Objects.equals(this.name, name) && matchesUuid(uuid);
    }

    public void draw(float x, float y) {
        if (this.texture != null && h()) {
            i();
        }
        Gui.drawRect(0xA0909090, x, y, x + 32.0F, y + 32.0F);
        ResourceLocation face = this.skin;
        if (face == null) {
            UUID id = EntityPlayer.getOfflineUUID(this.name != null ? this.name : "");
            face = net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(id); // Steve / Alex by UUID parity
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtil.drawHead(face, x, y);                                    // draws the 8x8 face of the skin scaled to 32x32
        this.drawTimer.lastMs = System.currentTimeMillis();
    }

    private static boolean isAlex(UUID id) {
        // (hi ^ lo) low bit, as DefaultPlayerSkin.isSlimSkin
        return (((int) ((id.getLeastSignificantBits() ^ id.getMostSignificantBits()) >> 32) ^ (int) (id.getLeastSignificantBits() ^ id.getMostSignificantBits())) & 1) != 0;
    }

    public void c() {
        Minecraft mc = Minecraft.getMinecraft();
        boolean validUuid = this.uuid != null && !this.uuid.isEmpty();
        if (validUuid && this.parsedUuid == null) {
            try {
                this.parsedUuid = UUIDTypeAdapter.fromString(this.uuid);
            } catch (IllegalArgumentException ignored) {
                validUuid = false;
            }
        }
        if (!validUuid) {
            if (this.parsedUuid == null) {
                this.parsedUuid = EntityPlayer.getOfflineUUID(this.name != null ? this.name : "");
            }
            if (!this.requested) {
                this.profile = new GameProfile(this.parsedUuid, this.name);
                this.requested = true;
            }
            return;
        }
        if (this.loaded) {
            return;
        }
        if (!this.requested) {
            AccountEntry.EXECUTOR.execute(() -> {
                try {
                    GameProfile p = new GameProfile(this.parsedUuid, this.name);
                    if (p.getId() != null) {
                        p = mc.getSessionService().fillProfileProperties(p, true);
                    }
                    this.profile = p;
                } catch (Exception e) {
                    this.requested = false;
                }
            });
            this.requested = true;
        }
        if (this.profile == null) {
            return;
        }
        MinecraftProfileTexture tex = mc.getSessionService().getTextures(this.profile, false).get(MinecraftProfileTexture.Type.SKIN);
        if (tex == null) {
            return;
        }
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        AccountEntry selected = manager.selectedAccount;
        if (selected != null && matchesUuid(selected.uuid)) {
            // remember the skin file name of the selected account in the skin cache table
            Adjust.INSTANCE.screenStack.preferences.skinTexture.setTexture(tex.getUrl(), FilenameUtils.getBaseName(tex.getUrl()));
        }
        this.texture = tex;
        i();
        this.loaded = true;
    }

    boolean h() {
        String key = "skins/" + FilenameUtils.removeExtension(FilenameUtils.getName(this.texture.getUrl()));
        ResourceLocation location = new ResourceLocation(key);
        Object loadedTexture = Minecraft.getMinecraft().getTextureManager().getTexture(location);
        return loadedTexture == net.minecraft.client.renderer.texture.TextureUtil.missingTexture;
    }

    /** Starts loading the skin through {@code SkinManager.loadSkin(texture, SKIN, callback)}; the callback stores the location in {@link #skin}. */
    public void i() {
        Minecraft.getMinecraft().getSkinManager().loadSkin(this.texture, MinecraftProfileTexture.Type.SKIN,
                (type, location, profileTexture) -> this.skin = location);
    }
}
