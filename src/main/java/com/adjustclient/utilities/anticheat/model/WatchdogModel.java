package com.adjustclient.utilities.anticheat.model;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import com.adjustclient.utilities.anticheat.model.check.*;
import com.adjustclient.utilities.anticheat.impl.WatchdogAntiCheat;
import com.adjustclient.utilities.anticheat.feature.ModuleFeature;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.repository.AccountRepository;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.SoundUtil;
import net.minecraft.util.IChatComponent;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.command.NumberInvalidException;
import java.util.*;
import java.util.stream.IntStream;
import com.adjustclient.modules.impl.combat.killaura.KillAuraModule;
import com.adjustclient.core.events.ScreenEventPhase;

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
                        com.adjustclient.utilities.anticheat.feature.Features.SCAFFOLD_NO_ASCEND,
                        com.adjustclient.utilities.anticheat.feature.Features.NO_SLOW_HYPIXEL,
                        com.adjustclient.utilities.anticheat.feature.Features.GAME_SPEED_HYPIXEL));
    }

    public void onActivate(com.adjustclient.core.events.DisconnectTeardownEvent event) {
        var manager = Adjust.INSTANCE.accountManager;
        Collection<AccountEntry> accounts = null;
        if (manager.currentRepository != null) {
            String playerId = net.minecraft.client.Minecraft.getMinecraft().getSession().getPlayerID();
            accounts = manager.currentRepository.findAccountsByUuid(playerId);
        }
        handleDisconnect(event, accounts);
    }

    public void handleDisconnect(com.adjustclient.core.events.DisconnectTeardownEvent event,
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
            if ("Ciyuna".equals(Adjust.username) || "shadow".equals(Adjust.username)) {
                SoundUtil.play("bedwars.banned", 1.0F, 1.0F);
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
        new com.adjustclient.utilities.backend.api.request.repository.SetPermissionsRequest(
                instant).toPacket().sendAsync(null);
    }

    @SuppressWarnings("unused")
    private static void vendorReportBanTelemetry(IChatComponent reason, long expiry) {
        if (Adjust.telemetryListener == null || reason == null) return;
        com.adjustclient.managers.telemetry.TelemetryQueue queue = com.adjustclient.managers.telemetry.TelemetryQueue.INSTANCE;
        if (queue == null) return;
        com.adjustclient.managers.telemetry.TelemetryWriter profile = queue.createProfile();
        if (profile != null && profile.recordCount >= 2) {
            com.adjustclient.managers.telemetry.TelemetryEventType eventType =
                    com.adjustclient.managers.telemetry.TelemetryEventType.match(reason.getUnformattedText());
            int remainingSeconds = expiry == -1L ? 0 : (int) ((expiry - System.currentTimeMillis()) / 1000L);
            Adjust.telemetryListener.report(new com.adjustclient.managers.telemetry.event.BytesRecord(
                    com.adjustclient.managers.telemetry.TelemetryFlag.SERVER,
                    com.adjustclient.managers.telemetry.TelemetryCategory.EXTREME,
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
                && com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveModule.INSTANCE.isEnabled()) {
            com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveMode mode =
                    com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveModule.INSTANCE.mode.getValue();
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
