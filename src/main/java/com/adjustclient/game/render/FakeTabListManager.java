package com.adjustclient.game.render;

import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.world.WorldSettings;

public final class FakeTabListManager implements Listener, MinecraftInstance {
    public Object2ObjectOpenHashMap<UUID, GameProfile> profiles = new Object2ObjectOpenHashMap<>();
    public Object field8;

    public void add(GameProfile profile, int ping) {
        synchronized (profiles) {
            if (profiles.containsKey(profile.getId())) return;
            profiles.put(profile.getId(), profile);
            if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
                dispatch(S38PacketPlayerListItem.Action.ADD_PLAYER, profile, ping);
                dispatch(S38PacketPlayerListItem.Action.UPDATE_LATENCY, profile, ping);
                dispatch(S38PacketPlayerListItem.Action.UPDATE_GAME_MODE, profile, ping);
            }
        }
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || event.outgoing) return;
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame) {
            synchronized (profiles) {
                for (GameProfile profile : profiles.values()) dispatch(S38PacketPlayerListItem.Action.REMOVE_PLAYER, profile, 0);
                profiles.clear();
            }
        } else if (packet instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem list = (S38PacketPlayerListItem) packet;
            if (list.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                synchronized (profiles) {
                    for (S38PacketPlayerListItem.AddPlayerData entry : list.getEntries()) profiles.remove(entry.getProfile().getId());
                }
            }
        }
    }

    public void remove(UUID id) {
        synchronized (profiles) {
            GameProfile profile = profiles.remove(id);
            if (profile != null && mc.thePlayer != null && mc.thePlayer.sendQueue != null)
                dispatch(S38PacketPlayerListItem.Action.REMOVE_PLAYER, profile, 0);
        }
    }

    public static void dispatch(S38PacketPlayerListItem.Action action, GameProfile profile, int ping) {
        if (!mc.isCallingFromMinecraftThread()) {
            mc.addScheduledTask(() -> dispatch(action, profile, ping));
            return;
        }
        S38PacketPlayerListItem packet = new S38PacketPlayerListItem();
        packet.action = action;
        packet.players = List.of(packet.new AddPlayerData(profile, ping,
                WorldSettings.GameType.ADVENTURE, null));
        mc.thePlayer.sendQueue.handlePlayerListItem(packet);
        ServerTracker.INSTANCE.playerTracker.onPacket(packet);
    }
}
