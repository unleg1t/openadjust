package com.adjustclient.util.anticheat.model;

import com.adjustclient.Adjust;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.adjustclient.util.anticheat.model.check.*;
import com.adjustclient.anticheat.impl.WatchdogAntiCheat;
import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.repository.AccountRepository;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import net.minecraft.util.IChatComponent;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.command.NumberInvalidException;
import java.util.*;
import java.util.stream.IntStream;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;
import com.adjustclient.event.ScreenEventPhase;

public final class WatchdogModel extends AntiCheatModel {
    public boolean ipBanMode;
    public Object field24;

    public WatchdogModel(boolean ipBanMode) { this(ServerType.HYPIXEL, ipBanMode); }

    public WatchdogModel(ServerType serverType, boolean ipBanMode) {
        super(serverType, new ScoreboardCheck(), new ChatFormatCheck(), new EntityStatusCheck(),
                new JoinGameCheck(), new InternalErrorCheck(), new TransactionDelayCheck(), new PayloadCheck());
        field8 = "/wdr %s";
        this.ipBanMode = ipBanMode;
        byte[] ids = {0, 3, 2, 4, 1, 5, 6};
        for (int n = 0; n < checks.length; n++) checks[n].id = ids[n];
    }

    public void setServerData(BlockerState server) {
        super.setServerData(server);
        if (server.featureBlocker.getAntiCheat(WatchdogAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(WatchdogAntiCheat.class,
                new WatchdogAntiCheat(WatchdogModel::isRestrictionBypassed,
                        com.adjustclient.anticheat.feature.Features.SCAFFOLD_NO_ASCEND,
                        com.adjustclient.anticheat.feature.Features.NO_SLOW_HYPIXEL,
                        com.adjustclient.anticheat.feature.Features.GAME_SPEED_HYPIXEL));
    }

    public void onActivate(com.adjustclient.event.DisconnectTeardownEvent event) {
        var manager = Adjust.INSTANCE.accountManager;
        Collection<AccountEntry> accounts = null;
        if (manager.currentRepository != null) {
            String playerId = net.minecraft.client.Minecraft.getMinecraft().getSession().getPlayerID();
            accounts = manager.currentRepository.findAccountsByUuid(playerId);
        }
        handleDisconnect(event, accounts);
    }

    public void handleDisconnect(com.adjustclient.event.DisconnectTeardownEvent event,
                                 Collection<AccountEntry> accounts) {
        if (accounts == null || accounts.isEmpty()) return;
        IChatComponent message = event.reason;
        if (message == null) return;
        List<IChatComponent> components = message.getSiblings();
        if (components.isEmpty()) return;
        String first = components.get(0).getUnformattedText();
        if (first.equals("Failed to authenticate your connection!")) return;
        long expiry = 0L;
        boolean instant = false;
        if (first.contains("blocked") || first.contains("banned")) {
            if (first.startsWith("You are permanently banned from this server!")
                    || first.startsWith("Your account has been blocked.")) {
                expiry = -1L;
                instant = event.origin == ScreenEventPhase.SERVER;
            } else if (first.equals("You are temporarily banned for ")) {
                try {
                    expiry = System.currentTimeMillis() + parseBanDuration(components);
                    String duration = components.get(1).getUnformattedText();
                    instant = duration.contains("0h 0m 0s") || isInstantBan(duration);
                } catch (Exception exception) {
                    Logger.log(LogLevel.WARN, true, "Unable to parse ban expiry length: {}", new Object[]{exception});
                    return;
                }
            } else return;
            if (ipBanMode) {
                NotificationBuilder notification = new NotificationBuilder("Debug IPBan");
                notification.addText("Set ip ban", (int) 4290690237L);
                if (instant) {
                    notification.append(" (INSTANT)");
                }
            }
        }
        for (AccountEntry account : accounts) {
            // setExpiry handles "Ban expiry for {} went under! {}" and marks the server dirty.
            account.bans.setExpiry(serverType, expiry, true);
            account.syncBans();
        }
    }

    @SuppressWarnings("unused")
    private static void vendorReportIpBan(boolean instant) {
        new com.adjustclient.backend.api.request.repository.SetPermissionsRequest(
                instant).toPacket().sendAsync(null);
    }

    @SuppressWarnings("unused")
    private static void vendorReportBanTelemetry(IChatComponent reason, long expiry) {
        if (Adjust.telemetryListener == null || reason == null) return;
        com.adjustclient.service.telemetry.TelemetryQueue queue = com.adjustclient.service.telemetry.TelemetryQueue.INSTANCE;
        if (queue == null) return;
        com.adjustclient.service.telemetry.TelemetryWriter profile = queue.createProfile();
        if (profile != null && profile.recordCount >= 2) {
            com.adjustclient.service.telemetry.TelemetryEventType eventType =
                    com.adjustclient.service.telemetry.TelemetryEventType.match(reason.getUnformattedText());
            int remainingSeconds = expiry == -1L ? 0 : (int) ((expiry - System.currentTimeMillis()) / 1000L);
            Adjust.telemetryListener.report(new com.adjustclient.service.telemetry.event.BytesRecord(
                    com.adjustclient.service.telemetry.TelemetryFlag.SERVER,
                    com.adjustclient.service.telemetry.TelemetryCategory.EXTREME,
                    eventType,
                    remainingSeconds));
        }
    }

    public static long parseBanDuration(List<IChatComponent> components) throws NumberInvalidException {
        long duration = 0L;
        for (String token : components.get(1).getUnformattedText().split(" ")) {
            char unit = token.charAt(token.length() - 1);
            int value = Integer.parseInt(token.substring(0, token.length() - 1));
            if (value < 0) throw new NumberInvalidException();
            switch (unit) {
                case 'd': duration += 86400000L * value; break;
                case 'h': duration += 3600000L * value; break;
                case 'm': duration += 60000L * value; break;
                case 's': duration += 1000L * value; break;
                default: break;
            }
        }
        if (duration < 1) throw new NumberInvalidException();
        return duration;
    }

    public static boolean containsAlmostOneDay(String text, int seconds) {
        return text.contains("23h 59m " + seconds + "s");
    }

    public boolean isInstantBan(String text) {
        return IntStream.iterate(60, seconds -> seconds - 1)
                .takeWhile(seconds -> seconds >= 0)
                .anyMatch(seconds -> containsAlmostOneDay(text, seconds));
    }

    public boolean allowsCapabilityC() {
        if ((KillAuraModule.INSTANCE.flags & 2) != 0
                && KillAuraModule.INSTANCE.targetSelector.rotationTarget != null
                && com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule.INSTANCE.isEnabled()) {
            com.adjustclient.module.impl.movement.inventorymove.InventoryMoveMode mode =
                    com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule.INSTANCE.mode.getValue();
        }
        var state = Adjust.gameStateTracker.state;
        return state.startTimer != null && !state.ended
                && !state.isSpectator() && (state.game.flags & 0x82) == 0;
    }

    public boolean allowsCapabilityD() { return true; }

    public static boolean isRestrictionBypassed() {
        var state = Adjust.gameStateTracker.state;
        return state.lobby == null || state.game == HypixelGame.HOUSING;
    }
}
