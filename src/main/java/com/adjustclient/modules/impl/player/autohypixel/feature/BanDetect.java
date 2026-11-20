package com.adjustclient.modules.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.LowLevelPacketReceiveEvent;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.core.settings.*;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.*;
import java.util.ArrayList;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class BanDetect extends AutoHypixelFeature {
    public static BanDetect INSTANCE;
    public final BooleanSetting revealUser;
    public final BooleanSetting disconnect;
    public long broadcastTime;
    public boolean awaitingRemoval;

    public BanDetect(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(owner, "Ban Detect");
        revealUser = new BooleanSetting(group, "Reveal user", true);
        revealUser.setDescription("Displays the player who disconnected when a ban was broadcast.");
        disconnect = new BooleanSetting(group, "Disconnect", false);
        disconnect.setDescription("Disconnect when someone in your game gets banned.");
        group.enabledPredicate = () -> revealUser.isEnabled() || disconnect.isEnabled();
    }

    @Override public boolean isFeatureEnabled() { return revealUser.isEnabled() || disconnect.isEnabled(); }

    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame) {
            awaitingRemoval = false;
        } else if (packet instanceof S02PacketChat) {
            S02PacketChat chat = (S02PacketChat) packet;
            if (chat.getType() == 2) return;
            IChatComponent message = chat.getChatComponent();
            if (message.getSiblings().size() != 1) return;
            IChatComponent first = message.getSiblings().getFirst();
            if (!first.getChatStyle().getBold() || first.getChatStyle().getColor() != ChatColor.RED
                    || !first.getUnformattedTextForChat().startsWith("A player has been removed from your ")) return;
            if (isActive() && disconnect.isEnabled() && Adjust.gameStateTracker.state.startTimer != null) {
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = owner.getName();
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.lifetimeSteps = 80;
                notification.addText("Returning to lobby because a player in your game was banned.", 0xFFBEBCBD).show();
                Adjust.chatMessageSender.sendMessage("/hub", false);
            }
            broadcastTime = System.currentTimeMillis();
            awaitingRemoval = true;
        } else if (packet instanceof S38PacketPlayerListItem) {
            if (!awaitingRemoval) return;
            S38PacketPlayerListItem list = (S38PacketPlayerListItem) packet;
            if (list.getAction() != S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                awaitingRemoval = false;
                return;
            }
            if (mc.theWorld == null) return;
            for (S38PacketPlayerListItem.AddPlayerData removed : list.getEntries()) {
                if (removed.getProfile() == null) continue;
                NetworkPlayerInfo info = mc.thePlayer.sendQueue.getPlayerInfo(removed.getProfile().getId());
                if (info == null || info.getResponseTime() != 1) continue;
                if (isActive() && revealUser.isEnabled()) {
                    ChatMessage message = owner.getFormattedName();
                    message.color = ChatColor.RED;
                    message.append(info.getGameProfile().getName());
                    message.color = ChatColor.GRAY;
                    message.append(" disconnected.");
                    ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
                }
                if (System.currentTimeMillis() - broadcastTime < 1200L) {
                }
                awaitingRemoval = false;
            }
        }
    }

    @SuppressWarnings("unused")
    private static void vendorReportBanBroadcast() {
        if (Adjust.telemetryListener == null) return;
        Adjust.telemetryListener.report(new com.adjustclient.managers.telemetry.event.EmptyRecord(
                com.adjustclient.managers.telemetry.TelemetryFlag.GAME,
                com.adjustclient.managers.telemetry.TelemetryCategory.MEDIUM));
    }

    @SuppressWarnings("unused")
    private static void vendorReportBanDetect(String removedPlayerName, long now) {
        com.adjustclient.managers.telemetry.TelemetryListener listener = Adjust.telemetryListener;
        if (listener == null || listener.state == null || !listener.state.enabled || listener.state.gameSent) {
            return;
        }
        com.adjustclient.managers.telemetry.TelemetryQueue queue = com.adjustclient.managers.telemetry.TelemetryQueue.INSTANCE;
        if (queue == null) return;
        com.adjustclient.managers.telemetry.TelemetryWriter profile = queue.createProfile();
        if (profile != null) {
            profile.add(new com.adjustclient.managers.telemetry.event.BanDetectRecord(
                    com.adjustclient.managers.telemetry.TelemetryFlag.GAME,
                    com.adjustclient.managers.telemetry.TelemetryCategory.MEDIUM,
                    removedPlayerName), now);
        }
    }
}
