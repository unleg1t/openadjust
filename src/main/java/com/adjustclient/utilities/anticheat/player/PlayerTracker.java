package com.adjustclient.utilities.anticheat.player;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.player.relation.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import com.mojang.authlib.GameProfile;
import com.adjustclient.utilities.anticheat.player.relation.PlayerRelation;

public final class PlayerTracker {
    public final LinkedHashMap<UUID, TabPlayer> players = new LinkedHashMap<>();
    public final LinkedHashMap<UUID, RelationCache> pending = new LinkedHashMap<>();
    private static final Object LOOKUP_PENDING = new Object();
    public final Object lock = new Object();
    public final Map<String, Object> names = new HashMap<>();
    public RelationResolver resolver;
    public boolean active;
    public Object field28;

    public void reconcile() {
        synchronized (lock) {
            if (!active) return;
            active = false;
            names.clear();
            UUID[] ids = players.entrySet().stream()
                    .filter(entry -> entry.getValue().realPlayer)
                    .map(entry -> entry.getKey())
                    .toArray(size -> new UUID[size]);
            for (UUID id : ids) {
                TabPlayer removed = players.remove(id);
                if (removed != null && removed.entry != null) HUDModule.INSTANCE_ALT.playerList.relayout();
                pending.remove(id);
            }
        }
    }
    public void onPacket(Packet<?> packet) {
        if (packet instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem tab = (S38PacketPlayerListItem) packet;
            if (tab.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) addPacket(tab);
            else if (tab.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) removePacket(tab);
        } else if (packet instanceof S3EPacketTeams) {
            if (resolver != null) synchronized (lock) {
                if (!pending.isEmpty()) onTeams((S3EPacketTeams) packet, resolver);
            }
            HUDModule.INSTANCE_ALT.playerList.needsSort = true;
        } else if (packet instanceof S01PacketJoinGame) {
            clear();
            updateResolver();
        } else if (packet instanceof S02PacketChat && ((S02PacketChat) packet).getType() != 2) {
            PlayerStatsCache.INSTANCE.onChat((S02PacketChat) packet);
        }
    }
    public void addPacket(S38PacketPlayerListItem packet) {
        Minecraft mc = Minecraft.getMinecraft();
        for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
            GameProfile profile = data.getProfile();
            if (profile == null || mc.thePlayer == null || mc.thePlayer.sendQueue == null) continue;
            NetworkPlayerInfo info = mc.thePlayer.sendQueue.getPlayerInfo(profile.getId());
            if (info == null || profile.getName() == null || !profile.getName().matches("^\\w{2,16}$")) continue;
            if (ServerTracker.INSTANCE.serverState != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == com.adjustclient.utilities.server.ServerType.HYPIXEL
                    && com.adjustclient.managers.servertracker.FriendServerTracker.isNpcName(info.getPlayerTeam())) continue;
            synchronized (lock) {
                TabPlayer existing = players.get(profile.getId());
                if (existing != null) {
                    if (existing.realPlayer) defer(profile.getId(), info);
                } else if (!pending.containsKey(profile.getId())) {
                    if (resolver == null || Objects.equals(profile.getId(), mc.getSession().getProfile().getId())) {
                        add(profile.getId(), info, false, null);
                    } else defer(profile.getId(), info);
                }
            }
        }
    }
    public void onTeams(S3EPacketTeams packet, RelationResolver resolver) {
        if (packet.getPlayers().isEmpty()) return;
        Map<UUID, PlayerRelation> results = null;
        for (UUID id : pending.keySet()) {
            RelationCache cache = pending.get(id);
            if (cache == null) continue;
            for (String name : packet.getPlayers()) {
                if (!Objects.equals(name, cache.info.getGameProfile().getName())) continue;
                ScorePlayerTeam team = cache.info.getPlayerTeam();
                if (team != null) {
                    PlayerRelation relation = resolver.resolve(cache, team);
                    if (relation != PlayerRelation.NO_ACTION) {
                        if (results == null) results = new HashMap<>();
                        results.put(id, relation);
                    }
                }
            }
        }
        resolveAll(results);
    }
    public void trackName(String name, Object relationData) {
        String key = name.toLowerCase(Locale.ROOT);
        synchronized (lock) {
            if (!active) return;
            for (TabPlayer player : players.values()) {
                if (player.info.getGameProfile().getName().equalsIgnoreCase(name)) return;
            }
            for (Map.Entry<UUID, RelationCache> entry : pending.entrySet()) {
                if (entry.getValue().info.getGameProfile().getName().equalsIgnoreCase(name)) {
                    add(entry.getKey(), entry.getValue().info, true, relationData);
                    return;
                }
            }
            if (names.putIfAbsent(key, LOOKUP_PENDING) != null) return;
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                String compact = com.adjustclient.utilities.WorldUtil.lookupUuid(name);
                return new GameProfile(UUID.fromString(compact.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), name);
            } catch (Exception failedLookup) {
                return null;
            }
        }).thenAccept(profile -> {
            if (profile == null) {
                synchronized (lock) { names.remove(key); }
            } else {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    acceptProfile(profile, name, relationData);
                });
            }
        });
    }
    public void acceptProfile(GameProfile profile, String requestedName, Object relationData) {
        if (ServerTracker.INSTANCE.playerTracker != this) return;
        synchronized (lock) {
            if (!active || names.remove(requestedName.toLowerCase(Locale.ROOT)) != LOOKUP_PENDING) return;
            for (TabPlayer player : players.values()) {
                if (player.info.getGameProfile().getName().equalsIgnoreCase(profile.getName())) return;
            }
            add(profile.getId(), new NetworkPlayerInfo(profile), true, relationData);
        }
    }
    public void updateResolver() {
        var server = ServerTracker.INSTANCE.serverState;
        RelationResolver next = null;
        if (server != null && server.antiCheatModel != null) {
            Object type = server.antiCheatModel.serverType;
            if (type == com.adjustclient.utilities.server.ServerType.HYPIXEL) next = new GameRelationResolver();
            else if (type == com.adjustclient.utilities.server.ServerType.LOYISA) next = new DefaultRelationResolver(500L);
            else if (type == com.adjustclient.utilities.server.ServerType.ANTICHEAT_TEST) next = new TeamRelationResolver();
        }
        if (resolver == next) return;
        if (resolver != null && next == null) synchronized (lock) {
            for (UUID id : pendingIds()) add(id, pending.get(id).info, false, null);
        }
        resolver = next;
    }
    public void clear() {
        synchronized (lock) {
            pending.clear();
            players.clear();
        }
        HUDModule.INSTANCE_ALT.playerList.relayout();
        PlayerStatsCache.INSTANCE.stats.clear();
    }
    public void add(UUID id, NetworkPlayerInfo info, boolean realPlayer, Object relationData) {
        remove(id, false);
        TabPlayer player = new TabPlayer(info, realPlayer);
        player.relationData = relationData;
        players.put(id, player);
        HUDModule.INSTANCE_ALT.playerList.onPlayerAdded(player);
    }
    public void defer(UUID id, NetworkPlayerInfo info) {
        remove(id, false);
        RelationCache cache = new RelationCache();
        cache.info = info;
        pending.put(id, cache);
        if (resolver != null) {
            ScorePlayerTeam team = info.getPlayerTeam();
            if (team != null) {
                PlayerRelation relation = resolver.resolve(cache, team);
                if (relation != PlayerRelation.NO_ACTION) resolve(id, relation);
            }
        }
    }
    public void removePacket(S38PacketPlayerListItem packet) {
        for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
            if (data.getProfile() != null) {
                synchronized (lock) { remove(data.getProfile().getId(), true); }
            }
        }
    }
    public void resolveAll(Map<UUID, PlayerRelation> relations) {
        if (relations == null) return;
        for (Map.Entry<UUID, PlayerRelation> entry : relations.entrySet()) {
            if (entry.getValue() == PlayerRelation.BOT_PLAYER) {
                remove(entry.getKey(), false);
            } else {
                RelationCache cache = pending.get(entry.getKey());
                add(entry.getKey(), cache.info, false, null);
            }
        }
    }
    public UUID[] pendingIds() {
        return pending.keySet().toArray(new UUID[0]);
    }
    public void remove(UUID id, boolean notify) {
        TabPlayer removed = players.remove(id);
        if (removed != null) {
            if (removed.entry != null) HUDModule.INSTANCE_ALT.playerList.relayout();
            if (notify) {
                EventBus.post(new com.adjustclient.core.events.TrackedPlayerRemovalEvent(removed));
            }
        }
        pending.remove(id);
    }
    public void resolve(UUID id, PlayerRelation relation) {
        if (relation == PlayerRelation.BOT_PLAYER) { remove(id, false); return; }
        RelationCache cache = pending.get(id);
        if (cache != null) add(id, cache.info, false, null);
        else remove(id, false);
    }
    public void expirePending() {
        if (resolver == null) return;
        synchronized (lock) {
            Map<UUID, PlayerRelation> results = null;
            for (UUID id : pending.keySet()) {
                RelationCache cache = pending.get(id);
                boolean expired;
                if (resolver instanceof GameRelationResolver) {
                    expired = cache.timer.lastMs + 5000L <= System.currentTimeMillis();
                } else if (resolver instanceof DefaultRelationResolver) {
                    expired = cache.timer.lastMs + ((DefaultRelationResolver) resolver).delay <= System.currentTimeMillis();
                } else {
                    net.minecraft.util.IChatComponent displayName = cache.info.getDisplayName();
                    expired = displayName != null && displayName.getSiblings().size() == 5;
                }
                if (expired) {
                    if (results == null) results = new HashMap<>();
                    results.put(id, PlayerRelation.VALID_PLAYER);
                }
            }
            resolveAll(results);
        }
    }
}
