package com.adjustclient.utilities.lunar.websocket;

import com.adjustclient.utilities.lunar.LunarClientService;
import com.adjustclient.modules.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.Session;

public final class LunarTabLogoCache {
    public final Set<UUID> requested = new HashSet<>();
    public LunarWebSocketClient client;
    public final Map<UUID, LunarTabLogo> logos = new ConcurrentHashMap<>();
    private Object field16;

    public void reset() {
        if (client != null) {
            client.stop();
        }
        requested.clear();
        logos.clear();
        client = new LunarWebSocketClient(this);
        client.reconnected =
                () -> requested.clear();
        client.start();
    }

    public void tick() {
        if (client != null && client.connected && client.socket != null
                && !client.socket.isInputClosed() && !client.socket.isOutputClosed()) {
            updatePlayers();
        }
    }

    private void updatePlayers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            return;
        }
        Map<String, NickedPlayer> nicked = PlayerDetectModule.INSTANCE.nickDetector == null
                ? Collections.emptyMap() : PlayerDetectModule.INSTANCE.nickDetector.nickedPlayers;
        Map<UUID, Object> present = new HashMap<>();
        UUID ownId = mc.thePlayer.getUniqueID();
        for (NetworkPlayerInfo info : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            NickedPlayer player = nicked.get(info.getGameProfile().getName());
            UUID id = player != null && player.realUuid != null
                    ? player.realUuid : info.getGameProfile().getId();
            if (!id.equals(ownId)) {
                present.put(id, Boolean.TRUE);
            }
        }
        List<UUID> added = new ArrayList<>();
        for (UUID id : present.keySet()) {
            if (!requested.contains(id)) {
                added.add(id);
            }
        }
        if (!added.isEmpty()) {
            requested.addAll(added);
            client.requestTabLogos(added);
        }
        List<UUID> removed = new ArrayList<>();
        for (UUID id : requested) {
            if (!present.containsKey(id)) {
                removed.add(id);
            }
        }
        if (!removed.isEmpty()) {
            removed.forEach(
                    id -> requested.remove(id));
            removed.forEach(
                    id -> logos.remove(id));
        }
    }

    public LunarTabLogo getTabLogo(UUID uuid) {
        return logos.get(uuid);
    }

    static Callable<String> jwtFetcher() {
        return () -> {
            for (;;) {
                try {
                    String token = LunarAuthenticator.fetchJwt();
                    UUID uuid = com.adjustclient.managers.session.SessionService.resolveSessionUuid(
                            Minecraft.getMinecraft().getSession());
                    LunarClientService.INSTANCE.jwt = token;
                    LunarClientService.INSTANCE.sessionUuid = uuid;
                    return token;
                } catch (Exception error) {
                    Thread.sleep(2000L);
                }
            }
        };
    }
}
