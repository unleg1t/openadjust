package com.adjustclient.utilities.hypixel.game.bedwars;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.HypixelGameStartedEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.game.*;
import com.adjustclient.utilities.hypixel.game.duels.DuelsGameState;
import com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.PacketUtil;
import com.google.gson.JsonObject;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.*;
import com.adjustclient.modules.impl.render.blockoverlay.BlockOverlayModule;

public final class BedWarsGameState extends TabTrackingGameState implements TeamGame, AliveCountProvider {
    public Vec3 spawnPosition;
    public BedWarsMode mode;
    public Integer heightLimit;
    public String serverId;
    public final Set<BlockPos> knownPositions = new HashSet<>();
    public final Map<BlockPos, SideTimer> placements = new HashMap<>();
    public final Set<BlockPos> recentPositions = new HashSet<>();
    public final Set<BlockPos> pendingPositions = new HashSet<>();
    public BedWarsTimerProtocol timerProtocol;
    public BedWarsMapInfo mapInfo;
    public Map<String, BedWarsTeam> playerTeams;
    public Set<String> eliminatedPlayers;
    public boolean bossBarSpawned;
    public boolean inferredStart;
    public boolean positionsRequested;
    public boolean removalChat;
    public boolean fromDuels;
    public Object field108;
    public static JsonObject bedLocations;
    public static JsonObject heightLimits;

    static {
        bedLocations = maps == null ? null : maps.getAsJsonObject("bed_locations");
        heightLimits = maps == null ? null : maps.getAsJsonObject("height_limits").getAsJsonObject("bedwars");
    }

    public BedWarsGameState() {
        super(HypixelGame.BED_WARS);
        var server = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != com.adjustclient.utilities.server.ServerType.HYPIXEL) {
            throw new IllegalStateException("Unable to locate because we are not on Hypixel!");
        }
        Adjust.chatMessageSender.sendMessage("/locraw", false);
        locrawFuture = new CompletableFuture<>();
        locrawFuture.thenAccept(
            response -> {
                if (response != null && response.map != null) {
                    serverId = response.map;
                    requestPositions();
                }
            });
    }

    public boolean isLargeGame() { return mode != null && mode.largeGame; }
    public int getTrackingDelay() { return 60000; }

    public void requestPositions() {
        if (positionsRequested || serverId == null || mode == null) return;
        positionsRequested = true;
    }

    @SuppressWarnings("unused")
    private void vendorRequestPositions() {
        if (positionsRequested || serverId == null || mode == null) return;
        positionsRequested = true;
        com.adjustclient.utilities.backend.api.PendingRequest request =
                new com.adjustclient.utilities.backend.api.request.misc.UnidentifiedRequestFB(
                        (serverId + mode.ordinal()).toLowerCase()).toPacket();
        request.sendAsync(
            response -> {
                if (response.status != com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
                    if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.RATELIMITED
                            || response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.TIMED_OUT) {
                        positionsRequested = false;
                    }
                    return;
                }
                if (response.body instanceof com.adjustclient.utilities.backend.api.response.DataResponseBody) {
                    com.adjustclient.utilities.backend.api.response.DataResponseBody data =
                            (com.adjustclient.utilities.backend.api.response.DataResponseBody) response.body;
                    mc.addScheduledTask(
                        () -> vendorReadPositions(data));
                }
            });
    }

    public void onBlockState(com.adjustclient.core.events.BlockPlacedEvent placement) {
        Block block = placement.block;
        BlockPos pos = placement.pos;
        EnumFacing side = placement.facing;
        if (block instanceof BlockDoor || block.isReplaceable(mc.theWorld, pos)) return;
        placements.put(pos, new SideTimer(new Timer(), side.getOpposite()));
    }

    @Override public BedWarsGameState copyFrom(HypixelGameState previous) {
        super.copyFrom(previous);
        if (previous instanceof DuelsGameState) {
            bossBarSpawned = ((DuelsGameState) previous).bossBarSpawned;
            fromDuels = true;
        }
        return this;
    }

    @SuppressWarnings("unused")
    private void vendorReadPositions(com.adjustclient.utilities.backend.api.response.DataResponseBody body) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(body.value));
            int count = input.readInt();
            for (int i = 0; i < count; i++) knownPositions.add(BlockPos.fromLong(input.readLong()));
        } catch (Exception exception) {
            com.adjustclient.core.logging.Logger.debugStackTrace(exception, null);
        }
    }

    public boolean isNearSpawn(BlockPos pos) {
        return spawnPosition != null && pos.distanceSq(spawnPosition.xCoord,
                spawnPosition.yCoord, spawnPosition.zCoord) <= 512.0;
    }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (before) {
            if (packet instanceof S08PacketPlayerPosLook) {
                if (Boolean.FALSE.equals(lobby)) {
                    S08PacketPlayerPosLook position = (S08PacketPlayerPosLook) packet;
                    detectSpawn(position.getX(), position.getY(), position.getZ());
                }
                return;
            }
            if (packet instanceof S3FPacketCustomPayload && timerProtocol != null) {
                timerProtocol.onPayload((S3FPacketCustomPayload) packet);
                return;
            }
            if (packet instanceof S02PacketChat) {
                if (!hasStartedFor(1000L)) return;
                List<IChatComponent> siblings = ((S02PacketChat) packet).getChatComponent().getSiblings();
                if (siblings.size() != 1) return;
                IChatComponent message = siblings.get(0);
                ChatStyle style = message.getChatStyle();
                if (style.getColor() == ChatColor.RED && !style.getBold() && !style.getItalic()
                        && !style.getUnderlined() && !style.getObfuscated()
                        && style.getChatClickEvent() == null && style.getChatHoverEvent() == null
                        && message.getSiblings().isEmpty()) removalChat = true;
                return;
            }
            if (!(packet instanceof S23PacketBlockChange)) {
                if (PacketUtil.isEntityUpdate(packet)) {
                    removalChat = false;
                    recentPositions.clear();
                }
                return;
            }
            S23PacketBlockChange change = (S23PacketBlockChange) packet;
            if (!removalChat || change.getBlockState().getBlock() != Blocks.air) return;
            BlockPos pos = change.getBlockPosition();
            SideTimer placement = placements.get(pos);
            if (placement == null) return;
            var samples = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE
                    .serverState.packetWatcher;
            int delay = samples.delayCount == 0 ? 0 : samples.totalDelay / samples.delayCount;
            if (placement.time().hasElapsed(delay + 40L)) return;
            BlockPos adjacent = pos.offset(placement.side());
            if (recentPositions.contains(adjacent)) return;
            recentPositions.add(pos);
            if (knownPositions.contains(pos)) return;
            knownPositions.add(pos);
            if (serverId == null || mode == null) pendingPositions.add(pos);
            return;
        }
        if (!(packet instanceof S3EPacketTeams) || lobby == null || lobby) return;
        S3EPacketTeams team = (S3EPacketTeams) packet;
        if (playerTeams != null && team.getPlayers() != null && !team.getPlayers().isEmpty()) {
            for (String player : team.getPlayers()) {
                if (team.getAction() == 4) {
                    playerTeams.remove(player.toLowerCase());
                    continue;
                }
                if (eliminatedPlayers.contains(player) || playerTeams.containsKey(player)
                        || !player.matches("^\\w{2,16}$")) continue;
                ScorePlayerTeam actual = mc.theWorld.getScoreboard().getTeam(team.getName());
                if (actual == null) continue;
                BedWarsTeam color = BedWarsTeam.byTeamPrefix(actual);
                if (color != null) playerTeams.put(player.toLowerCase(), color);
            }
        }
        if (startTimer != null && !fromDuels) return;
        String text = team.getPrefix() + team.getSuffix();
        if (mode == null && (fromDuels ? "team_5" : "team_4").equals(team.getName()) && text.contains(": §a")) {
            detectMode(StringUtils.stripControlCodes(text.split(": §a")[1]));
        }
        if (!fromDuels && "team_9".equals(team.getName()) && text.contains(": §a")) {
            String key = StringUtils.stripControlCodes(text.split(": §a")[1]).toLowerCase(Locale.ROOT).replaceAll(" ", "_");
            if (heightLimits != null && heightLimits.has(key)) heightLimit = heightLimits.get(key).getAsInt();
        }
    }

    public void detectSpawn(double x, double y, double z) {
        if (startTimer == null || spawnPosition != null) return;
        if (inferredStart) {
            if (!startTimer.hasElapsed(8500L) || hasStartedFor(12500L)) return;
        } else if (startTimer.hasElapsed(3000L)) return;
        if (x % 0.5 != 0 || z % 0.5 != 0) return;
        if (!fromDuels && mc.thePlayer.getDistanceSq(x, y, z) <= 1024.0) {
            if (!inferredStart) return;
            if (mc.thePlayer.posX % 0.5 == 0 && mc.thePlayer.posY == y && mc.thePlayer.posZ % 0.5 == 0) return;
        }
        spawnPosition = new Vec3(x, y, z);
        BlockOverlayModule.INSTANCE.shouldRenderOverlay();
    }

    @Override public GamePlayer recordKill(String victim, String killer, ScorePlayerTeam team) {
        eliminatedPlayers.add(victim.toLowerCase());
        playerTeams.remove(victim.toLowerCase());
        String key = team.getTeamName();
        Set<NickedPlayer> killed = new HashSet<>();
        killed.add(new NickedPlayer(victim, resolvePlayer(victim)));
        if (teamKills.containsKey(key)) killed.addAll(teamKills.get(key));
        for (ScorePlayerTeam other : mc.theWorld.getScoreboard().getTeams()) {
            if (other != team && other.getMembershipCollection().size() == 1
                    && other.getColorPrefix().equals(team.getColorPrefix())) {
                teamKills.put(key, new HashSet<>(killed));
                return new GamePlayer(killed, false);
            }
        }
        BedWarsTeam eliminatedTeam = BedWarsTeam.byTeamPrefix(team);
        playerTeams.values().removeIf(
            candidate -> candidate == eliminatedTeam);
        teamKills.remove(key);
        return new GamePlayer(new HashSet<>(killed), true);
    }

    public BedWarsTeam findPlayerTeam(String name) {
        return playerTeams == null ? null : playerTeams.get(name.toLowerCase());
    }

    public void detectMode(String text) {
        for (BedWarsMode candidate : BedWarsMode.values()) {
            if (candidate.aliases.stream().anyMatch(
                    alias -> text.equals(alias))) {
                mode = candidate;
                requestPositions();
                return;
            }
        }
        for (BedWarsMode candidate : BedWarsMode.values()) {
            if (text.contains(candidate.displaySubstring)) {
                mode = candidate;
                requestPositions();
                return;
            }
        }
    }

    @Override public void onBossBarSpawn() { bossBarSpawned = true; }

    @Override public void onGameServer() {
        timerProtocol = new BedWarsTimerProtocol();
        if (!fromDuels) mapInfo = new BedWarsMapInfo(this);
        if (!bossBarSpawned) {
            HypixelGameState previous = Adjust.gameStateTracker.previousTrackedState;
            if (previous instanceof BedWarsGameState) {
                if (previous.ended) Adjust.gameStateTracker.previousTrackedState = null;
                else Adjust.gameStateTracker.state = previous;
            }
            inferredStart = true;
            if (startTimer == null) {
                startTimer = new Timer();
                onStarted();
            }
        }
        super.onGameServer();
    }

    @Override public void onStarted() {
        playerTeams = new HashMap<>();
        eliminatedPlayers = new HashSet<>();
    }

    @Override public void onUpdate() {
        if (serverId == null || mode == null) return;
        pendingPositions.clear();
    }

    @SuppressWarnings("unused")
    private void vendorOnUpdate() {
        if (serverId == null || mode == null) return;
        Iterator<BlockPos> iterator = pendingPositions.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            new com.adjustclient.utilities.backend.api.request.misc.UnidentifiedRequestFA(
                    (serverId + mode.ordinal()).toLowerCase(), pos.toLong()).toPacket().sendAsync(null);
            iterator.remove();
        }
    }

    @Override public Integer getAdditionalData() {
        if (mode == BedWarsMode.BED_RUSH_DUEL) return Integer.valueOf(101);
        return heightLimit;
    }

    record SideTimer(Timer time, EnumFacing side) {
        @Override public boolean equals(Object object) {
            return this == object || object instanceof SideTimer other && time == other.time && side == other.side;
        }
        @Override public int hashCode() { return 31 * Objects.hashCode(time) + Objects.hashCode(side); }
        @Override public String toString() {
            return "g[time=" + (time == null ? "null" : "com.adjustclient.a.i.e.t@" + Integer.toHexString(time.hashCode())) + ", side=" + side + "]";
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnGameServerTelemetry() {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameStart(new HypixelGameStartedEvent(this));
        }
    }
}
