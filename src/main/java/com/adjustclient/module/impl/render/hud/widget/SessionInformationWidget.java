package com.adjustclient.module.impl.render.hud.widget;

import com.adjustclient.event.Render2DEvent;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.hud.layout.Point2f;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatColor;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import com.adjustclient.Adjust;
import com.adjustclient.game.movable.Movable;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.session.SessionStats;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountHead;
import com.adjustclient.util.anticheat.ServerTracker;

public final class SessionInformationWidget extends HudWidget {

    private AccountHead account;

    public SessionInformationWidget(Movable movable) {
        super("Session Information", "Statistics of the account you are playing on.", movable);
    }

    static String formatDuration(long millis) {
        long seconds = millis / 1000L % 60L;
        long minutes = millis / 60000L;
        long hours = millis / 3600000L;
        long days = millis / 86400000L;
        if (days >= 1L) {
            return String.format("%dd %dh %dm", days, hours % 24L, minutes % 60L);
        }
        if (hours % 24L > 0L) {          // v9 > 24 * (v9 / 24)
            return String.format("%dh %dm %ds", hours % 24L, minutes % 60L, seconds);
        }
        if (minutes % 60L > 0L) {        // v6 > 60 * (v6 / 60)
            return String.format("%dm %ds", minutes % 60L, seconds);
        }
        return String.format("%ds", seconds);
    }

    /**
     * Keeps {@link #account} in sync with the Minecraft session (re-creates it when the user name or player id changed), derives its
     * UUID from the player id and makes sure its skin profile is loaded: accounts without player id get an offline profile with a
     * random UUID, others have their profile and skin texture looked up (asynchronously through the client executor).
     */
    private void updateAccount() {
        Minecraft mc = Minecraft.getMinecraft();
        net.minecraft.util.Session session = mc.getSession();
        if (session == null) {
            return;
        }
        AccountHead entry = this.account;
        if (entry == null || !session.getUsername().equals(entry.name) || !entry.matchesUuid(session.getPlayerID())) {
            entry = new AccountHead(session.getUsername(), session.getPlayerID());
            this.account = entry;
        }
        if (entry.parsedUuid == null) {
            UUID uuid;
            try {
                uuid = UUID.fromString(entry.uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            } catch (Exception e) {
                uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");   // player id missing
            }
            entry.parsedUuid = uuid;
        }
        String playerId = entry.uuid;
        if (playerId == null || playerId.isEmpty()) {
            if (!entry.requested) {
                // offline account: random UUID profile with the user name
                entry.profile = new GameProfile(UUID.randomUUID(), entry.name);
                entry.requested = true;
            }
            return;
        }
        if (entry.loaded) {                                              // textures loaded
            return;
        }
        if (!entry.requested) {
            final AccountHead requested = entry;
            AccountEntry.EXECUTOR.execute(() -> {
                try {
                    GameProfile loaded = new GameProfile(requested.parsedUuid, requested.name);
                    if (loaded.getId() != null) {
                        loaded = mc.getSessionService().fillProfileProperties(loaded, true);
                    }
                    requested.profile = loaded;
                } catch (Exception e) {
                    requested.requested = false;
                }
            });
            entry.requested = true;
            return;
        }
        GameProfile profile = entry.profile;
        if (profile == null) {
            return;
        }
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = mc.getSessionService().getTextures(profile, false);
        MinecraftProfileTexture skin = textures.get(MinecraftProfileTexture.Type.SKIN);
        if (skin == null) {
            return;
        }
        if (Adjust.INSTANCE.accountManager.selectedAccount != null
                && entry.matchesUuid(Adjust.INSTANCE.accountManager.selectedAccount.uuid)) {
            Adjust.INSTANCE.screenStack.preferences.skinTexture.setTexture(skin.getUrl(), FilenameUtils.getBaseName(skin.getUrl()));
        }
        entry.texture = skin;
        entry.i();
        entry.loaded = true;
    }

    /** Render2D handler. */
    @com.adjustclient.event.EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!this.isActive()) {
            return;
        }
        this.updateAccount();
        if (this.account == null) {
            return;
        }
        String name = ChatColor.BOLD + this.account.name;
        if (StreamerModule.nameMap != null) {                                  // Streamer module state: hide the real name
            name = StreamerModule.replaceName(name, this.account.name);
        }
        float width = Math.max(93.0F, (float) (int) (Fonts.TAHOMA_12.getStringWidth(name) * 1.42F + 35.0F));
        this.drawFrame(event.scaledResolution, width, 22.5F);

        UUID uuid = this.account.parsedUuid;
        String userName = this.account.name;
        SessionStats stats;
        Map<UUID, SessionStats> cacheOwner = Adjust.INSTANCE.sessionService.sessions;
        synchronized (cacheOwner) {
            Map<UUID, SessionStats> cache = cacheOwner;
            stats = cache.get(uuid);
            if (stats == null) {
                long now = System.currentTimeMillis();
                stats = new SessionStats(userName, now, now);
                cache.put(uuid, stats);
            }
            stats.name = userName;
        }

        com.adjustclient.util.Vec2f position = this.movable.updatePosition();
        float x = position.x;
        float y = position.y;
        Fonts.TAHOMA_12.drawString(name, x + 23.5F, y + 13.5F, new Color(215, 219, 213, 215).getRGB());

        float kdr = (float) stats.kills;
        if (stats.deaths != 0) {
            kdr = kdr / (float) stats.deaths;
        }
        String kdrText = NumberSetting.FORMAT.format((double) kdr) + " KDR";
        float right = x + this.movable.width - 3.0F;
        Fonts.TAHOMA_10.drawString(kdrText, right - Fonts.TAHOMA_10.getStringWidth(kdrText), y + 24.5F, new Color(230, 225, 220, 235).getRGB());

        long playTime = stats.totalTime;
        if (ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
            playTime = stats.hypixelTime;
        }
        Fonts.TAHOMA_10.drawString(formatDuration(playTime), x + 22.5F, y + 24.5F, new Color(215, 219, 213, 215).getRGB());

        // kills / (kills + deaths) bar
        float barWidth = this.movable.width - 25.5F;
        float barLeft = x + 22.5F;
        float barTop = y + 20.0F;
        float barBottom = barTop + 1.0F;
        float killFraction = (float) stats.kills / (float) (stats.deaths + stats.kills);
        NvgDraw.fillRoundedRect(new Color(162, 162, 162, 140).getRGB(), barLeft, barTop, barLeft + barWidth, barBottom, 1.0F);
        NvgDraw.fillRoundedRect((HUDModule.getHudColor().getRGB() & 0xFFFFFF) | 0xC3000000, barLeft, barTop, killFraction * barWidth + barLeft, barBottom, 1.0F);

        final float headX = x;
        final float headY = y;
        event.postTasks.add(() -> this.drawHead(headX, headY));
    }

    /** Draws the account head with plain GL at (x + 4, y + 12.5), 16 px high (drawn at half scale, positions doubled). */
    private void drawHead(float x, float y) {
        float hx = x + 4.0F;
        float hy = y + 12.5F;
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.account.draw(hx * 2.0F, hy * 2.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }
}
