package com.adjustclient.utilities.hypixel;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

public final class TabListTracker implements Listener {
    public final Map<UUID, Entry> entries = new HashMap<>();
    public Object field8;

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (event.outgoing || !(event.packet instanceof S38PacketPlayerListItem)) return;
        S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
        if (packet.getAction() != S38PacketPlayerListItem.Action.UPDATE_LATENCY) return;
        synchronized (entries) {
            for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                Entry entry = entries.computeIfAbsent(data.getProfile().getId(),
                        id -> new Entry());
                int ping = data.getPing();
                if (ping < 1) continue;
                if (ping < entry.min) entry.min = ping;
                if (ping > entry.max) entry.max = ping;
                entry.count++;
                entry.average += (int) ((double) (ping - entry.average) / entry.count);
            }
        }
    }

    public Integer getAveragePing(UUID id) {
        synchronized (entries) {
            Entry entry = entries.get(id);
            return entry == null ? null : Integer.valueOf(entry.average);
        }
    }

    public boolean isActive() {
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL) {
            return false;
        }
        Boolean lobby = Adjust.gameStateTracker.state.lobby;
        return lobby != null && lobby;
    }

    public static class Entry {
        public int min = Integer.MAX_VALUE;
        public int max;
        public int average;
        public int count;
        public Object field20;
    }
}
