package com.adjustclient.hypixel.game;

import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import java.util.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;

public abstract class TabTrackingGameState extends HypixelGameState {
    public final Map<String, UUID> removedPlayers = new HashMap<>();
    public final Map<String, Set<NickedPlayer>> teamKills = new HashMap<>();

    public TabTrackingGameState(HypixelGame game) { super(game); }

    public UUID resolvePlayer(String name) {
        for (NetworkPlayerInfo info : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            if (name.equals(info.getGameProfile().getName())) return info.getGameProfile().getId();
        }
        UUID result = removedPlayers.get(name);
        removedPlayers.clear();
        return result;
    }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before || startTimer == null || mc.thePlayer == null || mc.thePlayer.sendQueue == null
                || !(packet instanceof S38PacketPlayerListItem)) return;
        S38PacketPlayerListItem list = (S38PacketPlayerListItem) packet;
        if (list.getAction() != S38PacketPlayerListItem.Action.REMOVE_PLAYER) return;
        for (S38PacketPlayerListItem.AddPlayerData data : list.getEntries()) {
            NetworkPlayerInfo info = mc.thePlayer.sendQueue.getPlayerInfo(data.getProfile().getId());
            if (info != null && info.getResponseTime() == 1) {
                removedPlayers.put(info.getGameProfile().getName(), info.getGameProfile().getId());
            }
        }
    }

    public GamePlayer recordKill(String victim, String killer, ScorePlayerTeam team) {
        String key = team.getTeamName();
        Set<NickedPlayer> players = new HashSet<>();
        players.add(new NickedPlayer(victim, resolvePlayer(victim)));
        if (teamKills.containsKey(key)) players.addAll(teamKills.get(key));
        boolean lastPlayer = team.getMembershipCollection().size() == 1;
        if (lastPlayer) teamKills.remove(key);
        else teamKills.put(key, new HashSet<>(players));
        return new GamePlayer(new HashSet<>(players), lastPlayer);
    }
}
