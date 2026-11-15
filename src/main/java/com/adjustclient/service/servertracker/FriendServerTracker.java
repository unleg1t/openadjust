package com.adjustclient.service.servertracker;

import com.adjustclient.Adjust;
import com.adjustclient.event.Listener;
import com.adjustclient.property.UserManager;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.BackendMessageHandler;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatColor;
import net.minecraft.scoreboard.Team;

public final class FriendServerTracker implements Listener, BackendMessageHandler {
    public Timer timer = new Timer();
    public IntArrayList reportedNames = new IntArrayList();
    public Map<String, ClientUser> users = new HashMap<>();
    public Object field16;

    public void onDisconnect() {
        timer.reset();
        clear();
    }

    public void handleTrackedFriends(List<TrackedFriend> players) {
        NotificationBuilder notification = new NotificationBuilder("Server Tracker");
        notification.addText("You are in the same ", 0xFFBEBCBD);
        String scope = "server as ";
        var server = ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == ServerType.HYPIXEL) {
            Boolean lobby = Adjust.gameStateTracker.state.lobby;
            if (lobby != null && !lobby) scope = "game as ";
        }
        notification.addText(scope, 0xFFBEBCBD);
        UserManager manager = (UserManager) Adjust.INSTANCE.field40;
        for (int i = 0; i < players.size(); i++) {
            TrackedFriend player = players.get(i);
            ClientUser user = manager.getOrCreate(player.server);
            users.put(player.name.toLowerCase(), user);
            notification.append(player.server);
            if (player.hidden) notification.addText(" (hidden)", 0xFFBEBCBD);
            if (i < players.size() - 1) notification.addText(i == players.size() - 2 ? " and " : ", ", 0xFFBEBCBD);
        }
        notification.addText(".", 0xFFBEBCBD);
        notification.lifetimeSteps = 140;
        notification.clearable = true;
        notification.show();
    }

    public void onConnect() {
        timer.reset();
        clear();
    }

    public void sendServer() {
        clear();
        timer.reset();
    }

    @SuppressWarnings("unused")
    private void vendorSendServer() {
        clear();
        timer.reset();
        Minecraft mc = Minecraft.getMinecraft();
        var server = ServerTracker.INSTANCE.serverState.antiCheatModel;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeInt(0);
            output.writeBoolean(server != null);
            if (server != null) output.writeInt(server.serverType.id);
            output.writeUTF(mc.getSession().getUsername());
            byte[] packet = bytes.toByteArray();
            packet[0] = 41;
            ServiceUtil.sendToBackend(packet);
        } catch (IOException exception) {
            throw new RuntimeException("SS", exception);
        }
    }

    public void sendLeave() {
        clear();
    }

    @SuppressWarnings("unused")
    private void vendorSendLeave() {
        clear();
        ServiceUtil.sendToBackend(new byte[]{42, 0, 0, 0});
    }

    @SuppressWarnings("unused")
    private void vendorOnPacket(com.adjustclient.event.DirectionalPacketEvent event) {
        if (event.outgoing) return;
        if (event.packet instanceof S01PacketJoinGame) { clear(); return; }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || !timer.hasElapsed(500L)) return;
        if (Adjust.hydraConnection == null || !Adjust.hydraConnection.authenticated || !Adjust.hydraConnection.ready) return;
        timer.reset();
        if (mc.thePlayer.sendQueue != null && (!mc.isIntegratedServerRunning() || mc.getIntegratedServer() == null)) {
            vendorReportPlayers(mc.thePlayer.sendQueue.getPlayerInfoMap());
        }
    }

    public ClientUser findUser(String name) {
        return users.get(name.toLowerCase());
    }

    public static boolean isNpcName(ScorePlayerTeam team) {
        return team != null && team.getNameTagVisibility() == Team.EnumVisible.NEVER
                && team.getColorPrefix().matches("^§[8a]\\[NPC] $");
    }

    public void reportPlayers(Collection<NetworkPlayerInfo> tabList) {
    }

    @SuppressWarnings("unused")
    private void vendorReportPlayers(Collection<NetworkPlayerInfo> tabList) {
        List<String> added = null;
        for (NetworkPlayerInfo info : tabList) {
            if (info == null || info.getGameProfile() == null || info.getGameProfile().getName() == null) continue;
            String name = info.getGameProfile().getName();
            if (!name.matches("^\\w{2,16}$") || name.equalsIgnoreCase(Minecraft.getMinecraft().getSession().getUsername())) continue;
            int hash = hashName(name.toLowerCase());
            if (reportedNames.contains(hash)) continue;
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == ServerType.HYPIXEL
                    && isNpcName(info.getPlayerTeam())) continue;
            reportedNames.add(hash);
            if (added == null) added = new ArrayList<>();
            added.add(name);
            if (added.size() >= 512) break;
        }
        if (added == null || added.isEmpty()) return;
        if (Adjust.username.equalsIgnoreCase("purity")
                || Adjust.INSTANCE.screenStack.preferences.booleanPreference.codec.value) {
            ServiceUtil.sendToBackend(com.adjustclient.backend.transport.HydraPacketEncoder.encodePlayerReport(added));
        }
    }

    public static int hashName(String name) {
        int hash = 977;
        for (byte value : name.getBytes(StandardCharsets.UTF_8)) hash = hash * 33 + value;
        return hash;
    }

    public void clear() {
        reportedNames.clear();
        users.clear();
        timer.reset();
    }
}
