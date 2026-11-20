package com.adjustclient.modules.impl.player.voicechat.hud;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.modules.impl.combat.antibot.AntiBotModule;
import com.adjustclient.modules.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceSessionManager;
import com.adjustclient.modules.impl.player.voicechat.playback.*;
import com.adjustclient.modules.impl.player.voicechat.soundboard.Soundboard;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.modules.impl.render.hud.widget.HudWidget;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.servertracker.FriendServerTracker;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.nanovg.NVGColor;
import static org.lwjgl.nanovg.NanoVG.*;
import net.minecraft.util.ChatColor;

public final class VoiceChatHud implements Listener, MinecraftInstance {
    public final VoiceChatModule module;
    public final Movable movable;

    public VoiceChatHud(VoiceChatModule module) {
        this.module = module;
        movable = Adjust.movableManager.registerMovable("voiceChat", new Vec2f(2.0f, 400.0f));
        movable.canDrag = () -> isVisible();
    }

    public boolean isVisible() { return module.isEnabled() && module.settings.hudEnabled.isEnabled(); }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        render();
    }

    public void render() {
        if (!module.isEnabled() || !module.settings.hudEnabled.isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        AudioStreamManager streams = module.getAudioStreamManager();
        List<VoiceHudEntry> entries = new ArrayList<>();
        AudioStream local = streams.getLocalStream();
        if (local != null || module.settings.displayAllPlayers.isEnabled()) entries.add(new VoiceHudEntry(mc.thePlayer, local));
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;
            AudioStream stream = streams.get(player.getUniqueID());
            if (player.getUniqueID() != null && module.getUserInfoCache().get(player.getUniqueID()) != null) entries.add(new VoiceHudEntry(player, stream));
            else if (module.settings.displayAllPlayers.isEnabled() && !AntiBotModule.isBot(player.getEntityId()))
                entries.add(new VoiceHudEntry(player, null));
        }
        float x = movable.updatePosition().x;
        float y = movable.updatePosition().y;
        float height = entries.size() * 8.0f + 8.0f;
        movable.width = 120.0f;
        movable.height = height + 9.0f;
        movable.headerHeight = 9.0f;
        int textColor = new Color(240, 240, 240, 222).getRGB();
        HudWidget.drawPanel(140, x, y, 120.0f, height);
        Fonts.TAHOMA_10.drawCenteredString("Voice Chat " + ChatColor.GRAY + entries.size(), x + 60.0f, y + 4.0f, textColor);
        y += 11.0f;
        VoiceSessionManager session = module.getSessionManager();
        if (session.client.udpClient == null || !session.client.udpClient.open.get()) {
            Fonts.TAHOMA_9.drawString("Not Connected", x + 2.0f, y + 1.0f, (int) 3741275477L);
            return;
        }
        if (!session.handler.authenticated) {
            Fonts.TAHOMA_9.drawString("Not Authenticated", x + 2.0f, y + 1.0f, (int) 3741275477L);
            return;
        }
        if (module.soundboard == null) module.soundboard = new Soundboard();
        String sound = module.soundboard.currentSound == null ? "(None)" : module.soundboard.currentSound.getName();
        Fonts.TAHOMA_9.drawString("Soundboard: " + sound, x + 2.0f, y + 1.0f, textColor);
        y += 8.0f;
        long vg = NvgContext.vg;
        for (VoiceHudEntry entry : entries) {
            EntityPlayer player = entry.player;
            AudioStream stream = entry.stream;
            float left = x + 2.0f;
            if (player.getUniqueID() != null && module.watchedPlayers.contains(player.getUniqueID())) {
                int red = new Color(255, 85, 85, 222).getRGB();
                Fonts.ICONS_11.drawString("m", left, y + 1.0f, red);
                Fonts.TAHOMA_9.drawString("x", left + 1.0f, y + 1.5f, red);
            } else if (stream != null && stream.isTalking()) {
                Fonts.ICONS_11.drawString("n", left, y + 1.0f, new Color(85, 255, 85, 222).getRGB());
            } else {
                Fonts.ICONS_11.drawString("m", left, y + 1.0f, new Color(85, 85, 85, 222).getRGB());
            }
            float nameX = left + 7.0f;
            String name = player.getDisplayName().getFormattedText();
            Fonts.TAHOMA_9.drawString(name, nameX, y + 2.0f, player == mc.thePlayer ? HUDModule.getHudColor().getRGB() : textColor);
            FriendServerTracker tracker = Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null
                    ? Adjust.INSTANCE.screenStack.serverTracker : null;
            ClientUser user = tracker != null && player.getGameProfile() != null && player.getGameProfile().getName() != null
                    ? tracker.findUser(player.getGameProfile().getName()) : null;
            if (user != null) Fonts.TAHOMA_9.drawString(" \u00a7l" + user.name, nameX, y + 2.0f, user.getColor());
            nvgBeginPath(vg);
            float[] levels = stream == null ? null : stream.levelMeter.levels;
            if (levels != null && levels.length >= 4) {
                int length = levels.length;
                int cursor = (int) Math.floorMod((System.currentTimeMillis() - stream.levelMeter.timer.lastMs) * (length / 1000L), (long) length);
                int count = length >> 2;
                int step = Math.max(1, count / 100);
                for (int i = 0; i < count; i += step) {
                    int index = i + cursor;
                    if (index >= length - step) index -= length - step;
                    index = Math.max(0, Math.min(length - 1, index));
                    float px = x + 78.0f + ((float) i / count) * 40.0f;
                    float py = y + 2.0f + 30.0f * levels[index];
                    if (i == 0) nvgMoveTo(vg, px, py); else nvgLineTo(vg, px, py);
                }
            } else {
                nvgMoveTo(vg, x + 78.0f, y + 2.0f);
                nvgLineTo(vg, x + 118.0f, y + 2.0f);
            }
            try (NVGColor color = NvgContext.color(textColor)) { nvgStrokeColor(vg, color); }
            nvgStrokeWidth(vg, 0.5f);
            nvgStroke(vg);
            nvgClosePath(vg);
            y += 8.0f;
        }
    }
}
