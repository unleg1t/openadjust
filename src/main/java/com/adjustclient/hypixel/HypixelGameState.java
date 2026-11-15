package com.adjustclient.hypixel;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.server.AbstractGameState;
import com.adjustclient.hypixel.game.LocrawResponse;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.telemetry.TelemetryRecord;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import net.minecraft.init.Items;

public class HypixelGameState extends AbstractGameState<HypixelGameState, HypixelGame> implements Listener, MinecraftInstance {
    public final HypixelGame game;
    public Timer createdTimer = new Timer();
    public Timer locationTimer = new Timer();
    public Timer startTimer;
    public Boolean lobby;
    public LocrawResponse locraw;
    public CompletableFuture<LocrawResponse> locrawFuture;
    public String mapName;
    public String mapTeam;
    public boolean tournament;
    public boolean spectator;
    public boolean ended;
    public boolean invisibilityEffect;
    public Object field44;
    public static JsonObject maps;

    public HypixelGameState(HypixelGame game) { super(game); this.game = game; }

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                mc.mcDefaultResourcePack.getInputStream(new ResourceLocation("adjust", "data/maps.json")),
                StandardCharsets.UTF_8))) {
            maps = new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to retrieve bed locations. This is bad!");
        }
        try { Class.forName(com.adjustclient.hypixel.game.zombies.round.ZombiesRoundData.class.getName()); }
        catch (ClassNotFoundException impossible) { throw new ExceptionInInitializerError(impossible); }
    }

    public void locate(boolean lobby) {
        this.lobby = lobby;
        if (!lobby && game != HypixelGame.UNKNOWN_GAME) {
            mc.addScheduledTask(() -> onGameServer());
        }
    }

    public HypixelGameState copyFrom(HypixelGameState previous) {
        startTimer = previous.startTimer;
        if (previous.startTimer != null) onStarted();
        tournament = previous.tournament;
        spectator = previous.spectator;
        lobby = previous.lobby;
        invisibilityEffect = previous.invisibilityEffect;
        ended = previous.ended;
        return this;
    }

    public void onChat(IChatComponent message) { }

    public void isTournamentMessage(IChatComponent message) {
        ChatStyle style = message.getChatStyle();
        if (style == null || style.getChatHoverEvent() == null) return;
        HoverEvent hover = style.getChatHoverEvent();
        if (hover.getAction() != HoverEvent.Action.SHOW_TEXT) return;
        IChatComponent value = hover.getValue();
        if (value.getChatStyle().getColor() == ChatColor.GRAY
                && "For more info, visit the Tournament Hall (/lobby TH)".equals(value.getUnformattedText())) {
            tournament = true;
        }
    }

    public void onPacket(Packet<?> packet, boolean before) {
        if (!before) {
            if (packet instanceof S30PacketWindowItems && lobby == null
                    && ((S30PacketWindowItems) packet).func_148911_c() == 0
                    && game.isAnyOf(HypixelGame.MEGA_WALLS, HypixelGame.THE_TNT_GAMES, HypixelGame.DUELS)) {
                locate(hasLobbyItem());
            }
            return;
        }
        if (packet instanceof S39PacketPlayerAbilities) {
            boolean mayFly = ((S39PacketPlayerAbilities) packet).isAllowFlying();
            if (!spectator && mayFly && invisibilityEffect) {
                invisibilityEffect = false;
                spectator = true;
                Logger.log(LogLevel.DEBUG, true, "In spectator mode.");
            } else if (spectator && !mayFly && mc.getRenderViewEntity() == mc.thePlayer) {
                spectator = false;
                Logger.log(LogLevel.DEBUG, true, "No longer in spectator mode.");
            }
        } else if (packet instanceof S1DPacketEntityEffect) {
            S1DPacketEntityEffect effect = (S1DPacketEntityEffect) packet;
            if (mc.thePlayer != null && !spectator && effect.getEffectId() == 14
                    && effect.getDuration() == Integer.MAX_VALUE && effect.getEntityId() == mc.thePlayer.getEntityId()) {
                invisibilityEffect = true;
            }
        } else if (packet instanceof S03PacketTimeUpdate) {
            invisibilityEffect = false;
        } else if (packet instanceof S1EPacketRemoveEntityEffect) {
            S1EPacketRemoveEntityEffect effect = (S1EPacketRemoveEntityEffect) packet;
            if (mc.thePlayer != null && spectator && effect.getEffectId() == 14
                    && effect.getEntityId() == mc.thePlayer.getEntityId()) {
                spectator = false;
                Logger.log(LogLevel.DEBUG, true, "No longer in spectator mode!");
            }
        } else if (packet instanceof S06PacketUpdateHealth) {
            if (!spectator && ((S06PacketUpdateHealth) packet).getHealth() <= 0) {
                Logger.log(LogLevel.DEBUG, true, "In spectator mode. (health)");
            }
        } else if (packet instanceof S13PacketDestroyEntities) {
            if (startTimer == null) {
                for (int id : ((S13PacketDestroyEntities) packet).getEntityIDs()) {
                    if (id == -1234) { detectGameStart(); break; }
                }
            }
        } else if (packet instanceof S02PacketChat) {
            if (!tournament && startTimer == null) isTournamentMessage(((S02PacketChat) packet).getChatComponent());
        } else if (packet instanceof S1CPacketEntityMetadata) {
            if (startTimer != null && !ended && (game.flags & (4 | 0x20)) == 0
                    && ((S1CPacketEntityMetadata) packet).getEntityId() == -1234) onGameEnd();
        } else if (packet instanceof S0FPacketSpawnMob) {
            if (((S0FPacketSpawnMob) packet).getEntityID() == -1234) onBossBarSpawn();
        } else if (packet instanceof S3EPacketTeams) {
            getScoreboardValue((S3EPacketTeams) packet);
            getServerIdLine((S3EPacketTeams) packet);
        }
    }

    public HypixelGameState current(HypixelGameState replacement) {
        HypixelGameState result = replacement.copyFrom(this);
        Adjust.gameStateTracker.setGame(result);
        return result;
    }

    public void getScoreboardValue(S3EPacketTeams packet) {
        if (lobby == null || lobby || !packet.getName().startsWith("team_")) return;
        if (mapTeam != null && !mapTeam.equals(packet.getName())) return;
        String text = packet.getPrefix() + packet.getSuffix();
        if (!text.contains(": §a")) return;
        String[] parts = text.split(": §a");
        if (parts.length < 2) return;
        String value = StringUtils.stripControlCodes(parts[1]);
        if (!org.apache.commons.lang3.StringUtils.isAlphanumericSpace(value.replaceAll("-", ""))) return;
        mapTeam = packet.getName();
        mapName = value;
        HypixelMapEvent event = new HypixelMapEvent(this, value);
        EventBus.post(event);
    }

    public void getServerIdLine(S3EPacketTeams packet) {
        if (lobby != null || packet.getColor() != 15 || packet.getFriendlyFlags() != 3
                || packet.getPlayers() != null && !packet.getPlayers().isEmpty()
                || !packet.getName().startsWith("team_")) return;
        String prefix = packet.getPrefix().trim();
        if (!prefix.contains("/24 ") && !prefix.contains("/25 ")
                && !prefix.contains("/26 ") && !prefix.contains("/27 ")) return;
        if (prefix.chars().filter(
                c -> c == '/').count() < 2) return;
        String server = StringUtils.stripControlCodes(prefix.substring(prefix.lastIndexOf(' ', prefix.length() - 1) + 1));
        if (!server.isEmpty()) locate(server.charAt(0) == 'L');
    }

    public void detectGameStart() {
        if (lobby == null || lobby) return;
        Logger.log(LogLevel.DEBUG, true, "Detected game start.");
        if (startTimer == null) {
            startTimer = new Timer();
            onStarted();
        }
        com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.playerList.updateColumns();
    }

    public void onBossBarSpawn() { }

    public HypixelGameState locate() {
        if ((game.flags & 0x40) != 0) locate(false);
        return this;
    }

    public void onGameServer() {
        if ((game.flags & 4) != 0 && startTimer == null) {
            startTimer = new Timer();
            onStarted();
        }
        com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.playerList.updateColumns();
    }

    public void onGameStart() {
        if (startTimer != null) return;
        startTimer = new Timer();
        onStarted();
    }

    public void onGameEnd() {
        Logger.log(LogLevel.DEBUG, true, "Detected game end.");
        ended = true;
        HypixelGameEndedEvent event = new HypixelGameEndedEvent(this);
        EventBus.post(event);
    }

    public void onStarted() { }

    public int isSpecialState() { return 0; }

    public boolean isSpectator() { return spectator; }

    public void onUpdate() { }

    public Object getAdditionalData() { return null; }

    public static boolean hasLobbyItem() {
        for (int slot = 0; slot < 9; slot++) {
            net.minecraft.item.ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            if (stack != null && !stack.getDisplayName().contains("§")) return false;
            if (slot == 0 && (stack == null || stack.getItem() != Items.compass)) return false;
            if (slot == 1 && (stack == null || stack.getItem() != Items.skull)) return false;
            if (slot == 7 && (stack == null || stack.getItem() != Items.dye)) return false;
            if (slot == 8 && (stack == null || stack.getItem() != Items.nether_star)) return false;
        }
        return true;
    }

    public void parseLocraw(DirectionalPacketEvent event) {
        if (event.outgoing || !(event.packet instanceof S02PacketChat)) return;
        IChatComponent message = ((S02PacketChat) event.packet).getChatComponent();
        if (!message.getSiblings().isEmpty() || message.getChatStyle().getColor() != ChatColor.WHITE) return;
        String text = message.getUnformattedText();
        if (!text.startsWith("{")) return;
        try {
            locraw = new Gson().fromJson(text, LocrawResponse.class);
            if (locrawFuture != null) {
                locrawFuture.complete(locraw);
                locrawFuture = null;
                event.cancelled = true;
            }
        } catch (Exception ignored) {
            // Unrelated JSON chat is not a location reply.
        }
    }

    public boolean hasStartedFor(long ms) { return startTimer != null && startTimer.hasElapsed(ms); }

    @SuppressWarnings("unused")
    private static void vendorMarkGameStateDirty() {
        if (ServerTracker.INSTANCE != null && Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameState();
        }
    }

    @SuppressWarnings("unused")
    private void vendorDetectGameStartTelemetry() {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameStart(new HypixelGameStartedEvent(this));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnGameServerTelemetry() {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameStart(new HypixelGameStartedEvent(this));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnGameStartTelemetry(TelemetryRecord startRecord) {
        if (Adjust.telemetryListener != null && startRecord != null) {
            Adjust.telemetryListener.report(startRecord);
        }
    }

    @SuppressWarnings("unused")
    private static void vendorOnGameEndTelemetry(HypixelGameEndedEvent event) {
        if (Adjust.telemetryListener != null && event != null) {
            Adjust.telemetryListener.onGameMode(event);
        }
    }
}
