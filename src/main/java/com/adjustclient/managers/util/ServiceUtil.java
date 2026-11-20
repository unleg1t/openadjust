package com.adjustclient.managers.util;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.transport.HydraConnection;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.impl.combat.antibot.AntiBotModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.managers.telemetry.BypassFeature;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import com.adjustclient.utilities.anticheat.model.AntiCheatModel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.core.commands.impl.debug.DebugEpsteinSubCommand;
import com.adjustclient.managers.credits.CreditsRenderer;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import net.minecraft.util.ChatColor;
import com.adjustclient.core.tag.PlayerTag;

public final class ServiceUtil {
    public static Runnable pendingSessionChange;
    public static com.adjustclient.core.gui.connection.ConnectionStatusOverlay connectionStatusOverlay;
    public static boolean ahmedElSisiMode;
    private static char[] substitutionCharacters;
    public static boolean pendingTasksReady;
    public static boolean recoveryRequested;
    public static boolean menuContext;
    public static boolean repositorySelectionPending;
    public static boolean modeOptionsApplied;
    public static boolean accountStatePending;
    public static boolean challengeAccepted = true;
    public static double movementAdjustment;
    public static int preferredConfigurationId;
    public static boolean userConfigurationWritable;
    public static List<Runnable> pendingTasks;
    public static Timer garbageCollectionTimer;
    public static Timer tagRequestTimer;
    public static boolean tagRequestInFlight;

    private static int vendorChallengeXor;
    private static int vendorIntegrityStage;
    private static int vendorIntegrityHits;
    private static int vendorIntegritySteps;
    private static boolean vendorChallengeVerified;
    private static boolean vendorShutdownRequested;
    private static boolean vendorReconnectImmediate;
    private static boolean vendorForceUpdateNotice;
    private static boolean vendorSessionChangeInFlight;
    private static boolean vendorUpdateNoticeArmed;
    private static com.adjustclient.utilities.Cooldown vendorUpdateCooldown;
    private static com.adjustclient.core.changelog.ChangelogLink vendorRemoteVersion;
    private static Timer vendorReconnectTimer;
    private static Thread vendorReconnectThread;
    private static boolean userConfigLoadPending;

    public static String decodeBackendAddress() {
        return "127.0.0.1";
    }

    @SuppressWarnings("unused")
    private static String vendorDecodeBackendAddress() {
        byte[] address = {(byte) -113, (byte) com.adjustclient.utilities.backend.transport.HydraMessageDispatcher.blacklistAddressByte, 75, 52};
        StringBuilder result = new StringBuilder();
        for (byte part : address) {
            if (result.length() != 0) result.append('.');
            result.append((part & 255) - 1);
        }
        return result.toString();
    }

    public static void sendServerIdentity() {
    }

    @SuppressWarnings("unused")
    private static void vendorSendServerIdentity() {
        try {
            AntiCheatModel model = ServerTracker.INSTANCE.serverState.antiCheatModel;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeInt(0);
            output.writeBoolean(model != null);
            if (model != null) output.writeInt(model.serverType.id);
            output.writeUTF(Minecraft.getMinecraft().getSession().getUsername());
            byte[] packet = bytes.toByteArray();
            packet[0] = 41;
            packet[1] = packet[2] = packet[3] = 0;
            vendorSendToBackend(packet);
        } catch (IOException exception) {
            throw new RuntimeException("SS", exception);
        }
    }

    public static boolean isPlayerStateRestricted() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return false;
    }

    public static void reportFeatureUse(BypassFeature feature) {
    }

    public static String formatAddress(BlockerState address) {
        String result = address.field12 + ":" + address.field48;
        if (result.length() >= 162) result = result.substring(0, 160);
        InetAddress resolved = address.field16;
        if (resolved != null) result += " " + resolved.getHostAddress();
        return result;
    }

    public static boolean isValidName(String name) {
        return name.length() >= 3 && name.matches("^[a-zA-Z0-9]*$") && name.length() <= 16;
    }

    public static boolean isRealPlayer(Entity entity) {
        return DebugEpsteinSubCommand.enabled && entity instanceof EntityPlayer
                && !(entity instanceof EntityPlayerSP) && !AntiBotModule.isBot(entity.getEntityId());
    }

    public static void closeOverlay(boolean scheduled, Runnable action) {
        if (!scheduled) {
            action.run();
        } else {
            synchronized (Minecraft.getMinecraft().scheduledTasks) {
                Minecraft.getMinecraft().scheduledTasks.add(
                        com.google.common.util.concurrent.ListenableFutureTask.create(() -> {
                            action.run();
                            return Boolean.FALSE;
                        }));
            }
        }
    }

    public static int mixIntegrity(int a, int b) {
        return 0;
    }

    public static void handleAccountInfo(byte[] data, int offset) {
    }

    public static void applyModeOptions(String[] names, int itemZLevel) {
        modeOptionsApplied = true;
        Category[] categories = Category.values();
        for (int i = 0; i < names.length; i++) {
            categories[i].name = names[i];
            int hash = 8717;
            for (char character : names[i].toCharArray()) hash = character * hash + 31;
            categories[i].field12 = hash;
        }
        Minecraft.getMinecraft().getRenderItem().zLevel = (float) itemZLevel;
    }

    public static void verifyChallenge(HydraConnection connection, int a, int b, int expected) {
    }

    public static void openMainMenu() {
        if (Adjust.username != null) Minecraft.getMinecraft().displayGuiScreen(new AdjustMainMenu());
    }

    public static void applyRemoteOption(int key, int value) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (key) {
            case 0x9162f95b: mc.fontRendererObj.forkField68 = value; break;
            case 0x0fab0a8a: mc.ingameGUI.forkField108 = value >= 33; break;
            case 0x8a71f922: mc.ingameGUI.armorIconLimit = value; break;
            default: break;
        }
    }

    public static boolean isValidDisplayName(String name) {
        return name.length() >= 3 && name.length() <= 26 && name.matches("^[a-zA-Z0-9-\\s]*$");
    }

    public static void requestUserData() {
    }

    public static void sendIntegrityChallenge() {
    }

    public static boolean isActiveModel(Object model) {
        BlockerState state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.antiCheatModel == null) return false;
        AntiCheatModel current = state.antiCheatModel;
        return current.serverType == model && current.isAllChecksPassed();
    }

    public static void sendToBackend(byte[] packet) {
    }

    @SuppressWarnings("unused")
    private static void vendorSendToBackend(byte[] packet) {
        if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) Adjust.hydraConnection.send(packet);
    }

    public static void collectGarbage(boolean force) {
        if (AdjustMainMenu.windowActive || Minecraft.getMinecraft() == null) return;
        if (!force && garbageCollectionTimer != null && !garbageCollectionTimer.hasElapsed(1000L)) return;
        long started = System.currentTimeMillis();
        System.gc();
        if (Logger.debug) Logger.log(LogLevel.DEBUG, true, "Explicit garbage clean: {}ms", System.currentTimeMillis() - started);
        if (garbageCollectionTimer == null) garbageCollectionTimer = new Timer();
        garbageCollectionTimer.reset();
    }

    public static int addServerDelay(int delay) {
        BlockerState state = ServerTracker.INSTANCE.serverState;
        if (state == null) return delay + 50;
        var watcher = state.packetWatcher;
        int average = watcher.delayCount == 0 ? 0 : watcher.totalDelay / watcher.delayCount;
        return delay + Math.max(average, 50);
    }

    public static void runPendingTasks() {
        pendingTasksReady = true;
        userConfigurationWritable = true;
        if (pendingTasks != null) {
            List<Runnable> tasks = pendingTasks;
            pendingTasks = null;
            for (Runnable task : tasks) task.run();
        }
        if (Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null && Adjust.INSTANCE.screenStack.configurations != null) {
            Adjust.INSTANCE.screenStack.configurations.initLocal();
        }
        if (com.adjustclient.managers.config.user.UserConfiguration.INSTANCE != null) {
            if (com.adjustclient.core.rendering.font.Fonts.TAHOMA != null
                    && com.adjustclient.core.rendering.font.Fonts.TAHOMA_12 != null) {
                userConfigLoadPending = false;
                com.adjustclient.managers.config.user.UserConfiguration.INSTANCE.loadLocal();
            } else {
                userConfigLoadPending = true;
            }
        }
        if (Adjust.INSTANCE != null && Adjust.INSTANCE.tagManager != null) {
            Adjust.INSTANCE.tagManager.loadLocal();
        }
    }

    public static void runWhenInitialized(Runnable action) {
        try {
            if (Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null) {
                action.run();
            } else {
                if (pendingTasks == null) pendingTasks = new ArrayList<>();
                pendingTasks.add(action);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static com.adjustclient.utilities.HolidayTheme resolveHolidayTheme() {
        if (com.adjustclient.core.commands.impl.debug.DebugSeasonSubCommand.holidayOverride != null)
            return com.adjustclient.core.commands.impl.debug.DebugSeasonSubCommand.holidayOverride;
        int month = Calendar.getInstance().get(Calendar.MONTH);
        if (month == 9) return com.adjustclient.utilities.HolidayTheme.HALLOWEEN;
        if (month == 11) return com.adjustclient.utilities.HolidayTheme.CHRISTMAS;
        return null;
    }

    public static void markConfigurationDirty() {
        Adjust.INSTANCE.fileSystem.markDirty();
    }

    public static void handleSerializationFailure() {
        Logger.log(LogLevel.ERROR, true, "Unable to serialize file system");
        System.exit(0);
    }

    public static boolean isRunningUnderWine() {
        for (String key : System.getenv().keySet()) {
            if (key.equalsIgnoreCase("wineusername") || key.equalsIgnoreCase("wineprefix")) return true;
        }
        return false;
    }

    public static void markDetectedPlayer(String name) {
        if (name.contains(ChatColor.OBFUSCATED.toString())) return;
        if ((Adjust.gameStateTracker.state.game.flags & 2) != 0) return;
        var manager = Adjust.INSTANCE.tagManager;
        if (manager.getTag(name) == com.adjustclient.core.tag.PlayerTag.WHITELIST) return;
        manager.setTag(name, com.adjustclient.core.tag.PlayerTag.TARGET).field12 = true;
    }

    public static final Runnable CONNECT = () -> {
    };

    @SuppressWarnings("unused")
    private static java.util.function.Consumer<com.adjustclient.utilities.backend.api.response.BackendResponse> vendorTagReply() {
        return response -> {
            if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK
                    && response.body instanceof com.adjustclient.utilities.backend.api.response.DataResponseBody) {
                Adjust.INSTANCE.tagManager.deserialize(((com.adjustclient.utilities.backend.api.response.DataResponseBody) response.body).value);
                tagRequestTimer = null;
            } else {
                tagRequestInFlight = false;
                Logger.log(LogLevel.ERROR, true, "Failed to receive tag data: {}", response);
            }
        };
    }

    public static final Runnable RESTART = () -> {
    };

    public static final Runnable IGNORE_UPDATE = () -> {
    };

    public static void updateServices() {
        if (userConfigLoadPending
                && com.adjustclient.core.rendering.font.Fonts.TAHOMA != null
                && com.adjustclient.core.rendering.font.Fonts.TAHOMA_12 != null
                && com.adjustclient.managers.config.user.UserConfiguration.INSTANCE != null) {
            userConfigLoadPending = false;
            com.adjustclient.managers.config.user.UserConfiguration.INSTANCE.loadLocal();
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null && mc.theWorld == null && pendingSessionChange != null) {
            pendingSessionChange.run();
            pendingSessionChange = null;
        }
    }

    public static void resetServiceState() {
        connectionStatusOverlay = null;
        var accounts = Adjust.INSTANCE.accountManager;
        Minecraft mc = Minecraft.getMinecraft();
        // The AccountManagerScreen clear-selection operation schedules its list clear on the Minecraft thread.
        mc.addScheduledTask(() -> accounts.selection.clear());
        if (accounts.searchField != null
                && !accounts.searchField.getText().isEmpty()) accounts.searchChanged = true;
        accounts.selectedAccount = null;
        if (accounts.viewButton != null) {
            accounts.viewButton.enabled = false;
            accounts.loginButton.enabled = false;
        }
        if (accounts.currentRepository != null) {
            accounts.currentRepository = null;
            accounts.selection.clear();
        }
        accounts.repositories.clear();
        ClientInfoCollector.entries.clear();
        if (Adjust.chatManager != null && Adjust.chatManager.globalChannel != null) {
            var chat = Adjust.chatManager.globalChannel;
            chat.pendingMessages = null;
            chat.locallySubscribed = false;
            chat.field48 = null;
        }
        for (com.adjustclient.managers.BackendMessageHandler service : Adjust.INSTANCE.screenStack.field52) {
            try {
                service.onDisconnect();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        accountStatePending = false;
        recoveryRequested = false;
        ahmedElSisiMode = false;
        CreditsRenderer.INSTANCE = null;
    }

    private static CharArrayList buildSubstitutionAlphabet() {
        char[] source = "محارب الله اسمي أحمد السيسي وأنا هاكر هاكر مخيف كبير لحماس أنا أحب الإنترنت واسمي أحمد السيسي أنا أصنع برامج تشفير للبرمجيات الخبيثة وأبيعها وأنا أعظم محارب لحماس أرجوكم اشتركوا معي اليوم هو يوم سعدي لأن اسمي أحمد وأحب الاختراق هاها أحمد السيسي".toCharArray();
        CharArrayList unique = new CharArrayList();
        for (char character : source) {
            if (!unique.contains(character)) {
                unique.add(character);
            }
        }
        return unique;
    }

    public static String substituteTextCharacters(String text) {
        if (substitutionCharacters == null) {
            substitutionCharacters = buildSubstitutionAlphabet().toCharArray();
        }
        char[] output = new char[text.length()];
        char salt = (char) (int) (movementAdjustment * 234.0);
        boolean afterSection = false;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            boolean isSection = character == '§';
            if (character >= ':' && (character < '[' || character >= 'a') && character < '{' && !afterSection) {
                output[i] = substitutionCharacters[(character ^ salt) % substitutionCharacters.length];
            } else {
                output[i] = character;
            }
            afterSection = isSection;
        }
        return new String(output);
    }

    public static long randomLong() {
        byte[] bytes = new byte[8];
        RandomUtil.RANDOM.nextBytes(bytes);
        return ByteBuffer.wrap(bytes).getLong();
    }

    @SuppressWarnings("unused")
    private static void vendorReportFeatureUse(
            BypassFeature feature,
            java.util.Map<BypassFeature, com.adjustclient.managers.telemetry.TelemetryRecord> reported,
            com.adjustclient.managers.telemetry.TelemetryRecord record,
            boolean bypassFlag) {
        if (reported == null || reported.containsKey(feature)) return;
        BlockerState state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.antiCheatModel == null
                || state.antiCheatModel.serverType != ServerType.HYPIXEL
                || !isActiveModel(ServerType.HYPIXEL)) {
            return;
        }
        if (Adjust.gameStateTracker == null || Adjust.gameStateTracker.state == null) return;
        boolean allow = bypassFlag || (Adjust.gameStateTracker.state.game == com.adjustclient.utilities.hypixel.HypixelGame.HOUSING);
        if (!allow) return;
        if (Adjust.telemetryListener != null && record != null) {
            Adjust.telemetryListener.report(record);
            reported.put(feature, record);
        }
    }

    @SuppressWarnings("unused")
    private static int vendorMixIntegrity(int a, int b) {
        int xor = a ^ b;
        int result = xor ^ 0x17DFC81;
        if (vendorIntegrityStage == 0) {
            for (int step = 0; step < 16; step++) {
                vendorIntegrityStage++;
                if (vendorChallengeXor == xor) {
                    vendorIntegrityHits++;
                    result += 32 * (xor ^ result ^ 0xE1F59E);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static void vendorVerifyChallenge(
            HydraConnection connection, int a, int b, int expected, int handshakeSeed) {
        int mixed = vendorMixIntegrity(a, b);
        challengeAccepted = true;
        int swappedSeed = Integer.reverseBytes(handshakeSeed);
        int encodedTag = 33 * (33 * (33 * (1089 * (mixed ^ swappedSeed) + 24) + 48) + 72) + 96;
        byte[] packet = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(2)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(mixed)
                .putInt(b)
                .putInt(encodedTag)
                .array();
        connection.send(packet);
        int expectedCheck = 33 * (33 * (33 * (33 * (33 * (1089 * (swappedSeed ^ a) + 19) + 38) + 57) + 76) + 95) + 114;
        if (expectedCheck != expected) {
            System.exit(0);
            return;
        }
        vendorChallengeVerified = true;
        vendorIntegritySteps++;
        applyRemoteOption(b, a);
    }

    @SuppressWarnings("unused")
    private static void vendorRequestUserData(boolean alreadySent) {
        repositorySelectionPending = false;
        if (!alreadySent && Adjust.hydraConnection != null) {
            try {
                byte[] packet = ByteBuffer.allocate(12)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(4)
                        .order(ByteOrder.BIG_ENDIAN)
                        .putInt((int) com.adjustclient.utilities.InitPhase.initializationMillis)
                        .putInt(0)
                        .array();
                Math.toIntExact(packet.length);
                Adjust.hydraConnection.send(packet);
            } catch (Exception exception) {
                Adjust.hydraConnection.checkTimeout();
                Logger.log(LogLevel.ERROR, true, "Getting user data", exception);
            }
        }
        for (com.adjustclient.managers.BackendMessageHandler service : Adjust.INSTANCE.screenStack.field52) {
            service.onConnect();
        }
    }

    @SuppressWarnings("unused")
    private static void vendorSendIntegrityChallenge(int tokenA, long tokenB, int tokenC) {
        Minecraft mc = Minecraft.getMinecraft();
        int hc = System.identityHashCode(mc);
        int v6 = com.adjustclient.core.gui.hud.information.Information.displayedCounter + hc;
        int v7 = 1 - com.adjustclient.core.gui.hud.information.Information.displayedCounter;
        if (mc.ingameGUI != null && Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) {
            byte[] packet = ByteBuffer.allocate(28)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(10)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putInt(v6)
                    .putInt(v7)
                    .putInt(tokenA)
                    .putLong(tokenB)
                    .putInt(tokenC)
                    .array();
            Adjust.hydraConnection.send(packet);
        }
    }

    @SuppressWarnings("unused")
    private static void vendorHandleAccountInfo(byte[] data, int offset) {
        if (data == null || offset < 0 || offset >= data.length) return;
        ByteBuffer buf = ByteBuffer.wrap(data, offset, data.length - offset);
        int skinIndex = buf.get() & 0xFF;
        com.adjustclient.managers.prefs.SkinModel[] skins = com.adjustclient.managers.prefs.SkinModel.values();
        com.adjustclient.managers.prefs.SkinModel selectedSkin =
                skinIndex < skins.length ? skins[skinIndex] : skins[0];
        if (com.adjustclient.modules.impl.render.hud.HUDModule.INSTANCE_ALT != null
                && com.adjustclient.modules.impl.render.hud.HUDModule.INSTANCE_ALT.invites != null) {
            com.adjustclient.modules.impl.render.hud.HUDModule.INSTANCE_ALT.invites.setMode(selectedSkin);
        }

        int configLen = buf.getInt();
        if (configLen >= 0 && configLen <= buf.remaining()) {
            byte[] configBytes = new byte[configLen];
            buf.get(configBytes);
            if (com.adjustclient.managers.config.user.UserConfiguration.INSTANCE != null) {
                com.adjustclient.managers.config.user.UserConfiguration.INSTANCE.load(configBytes);
            }
            userConfigurationWritable = true;
        }

        String username = readUtf16(buf);
        String token = readUtf16(buf);
        int[] trailing = new int[5];
        for (int i = 0; i < trailing.length && buf.remaining() >= 4; i++) {
            trailing[i] = buf.getInt();
        }
        int keyCount = buf.remaining() >= 4 ? buf.getInt() : 0;
        int queueSeed = 0;
        for (int i = 0; i < keyCount && buf.remaining() >= 4; i++) {
            int element = buf.getInt();
            if (i == 0) queueSeed = element;
        }
        if (com.adjustclient.managers.storage.LocalStorage.INSTANCE != null) {
            com.adjustclient.managers.storage.LocalStorage.INSTANCE.runKeyQueue(queueSeed);
        }

        Adjust.username = username;
        if ("wykt".equals(username) || "jdh".equals(username) || "Moonaw".equals(username)
                || "Fatal".equals(username) || "auth".equals(username)
                || "final".equals(username) || "purity".equals(username)) {
            com.adjustclient.managers.party.chat.QuickMessage.MESSAGES.add(
                    new com.adjustclient.managers.party.chat.ItemRequestQuickMessage("cp", null));
            RandomUtil.RANDOM.nextDouble();
        }
        com.adjustclient.utilities.HashUtil.hash(username + "9788");
        movementAdjustment = RandomUtil.RANDOM.nextDouble();
        preferredConfigurationId = trailing[0];
        if (token != null && !token.isEmpty()) {
            Float.parseFloat("0." + Math.abs(token.hashCode()));
        }
        vendorChallengeVerified = true;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof com.adjustclient.core.gui.registration.RecoveryKeyScreen) {
            mc.displayGuiScreen(new AdjustMainMenu());
        }
    }

    private static String readUtf16(ByteBuffer buf) {
        if (buf.remaining() < 4) return "";
        int byteLen = buf.getInt();
        if (byteLen <= 0 || byteLen > buf.remaining()) return "";
        byte[] raw = new byte[byteLen];
        buf.get(raw);
        return new String(raw, StandardCharsets.UTF_16BE);
    }

    @SuppressWarnings("unused")
    private static final Runnable vendorConnect = () -> {
        HydraConnection connection = Adjust.hydraConnection;
        if (connection != null) {
            connection.ready = false;
            connection.authenticated = false;
            if (vendorReconnectTimer == null) vendorReconnectTimer = new Timer();
            vendorReconnectTimer.lastMs = System.currentTimeMillis();
            vendorResetServiceState();
            connection.connect();
        }
        if (vendorReconnectThread != null) {
            vendorReconnectThread.interrupt();
            vendorReconnectThread = null;
        }
    };

    @SuppressWarnings("unused")
    private static final Runnable vendorRestart = () -> {
        Minecraft.getMinecraft().shutdown();
        if (vendorUpdateCooldown == null) vendorUpdateCooldown = new com.adjustclient.utilities.Cooldown(10_000_000L);
        vendorUpdateCooldown.timer.lastMs = System.currentTimeMillis();
        vendorUpdateCooldown.durationMs = 10_000_000L;
    };

    @SuppressWarnings("unused")
    private static final Runnable vendorIgnoreUpdate = () -> {
        if (vendorUpdateCooldown == null) vendorUpdateCooldown = new com.adjustclient.utilities.Cooldown(600_000L);
        vendorUpdateCooldown.timer.lastMs = System.currentTimeMillis();
        vendorUpdateCooldown.durationMs = 600_000L;
    };

    @SuppressWarnings("unused")
    private static void vendorUpdateServices() {
        HydraConnection connection = Adjust.hydraConnection;
        if (connection == null || !connection.authenticated) {
            if (!vendorShutdownRequested) {
                if (vendorReconnectTimer == null) vendorReconnectTimer = new Timer();
                long retryMs = vendorReconnectImmediate ? 300L : 3500L;
                if (vendorReconnectTimer.lastMs + retryMs <= System.currentTimeMillis()) {
                    vendorReconnectImmediate = false;
                    vendorReconnectTimer.lastMs = System.currentTimeMillis();
                    if (vendorReconnectThread != null) {
                        vendorReconnectThread.interrupt();
                        vendorReconnectThread = null;
                    }
                    Thread thread = new Thread(vendorConnect, "Hydra Connect");
                    vendorReconnectThread = thread;
                    thread.start();
                }
            }
            return;
        }
        if (vendorChallengeVerified) {
            if (!accountStatePending) {
                vendorRequestUserData(false);
                vendorSendIntegrityChallenge(0, 0L, 0);
                modeOptionsApplied = true;
                if (ServerTracker.INSTANCE.serverState != null) {
                    vendorSendServerIdentity();
                }
            }
            return;
        }
        if (vendorRemoteVersion != null && vendorUpdateNoticeArmed) {
            if (vendorUpdateCooldown == null || vendorUpdateCooldown.isExpired()) {
                if (vendorUpdateCooldown == null) vendorUpdateCooldown = new com.adjustclient.utilities.Cooldown(5000L);
                vendorUpdateCooldown.timer.lastMs = System.currentTimeMillis();
                vendorUpdateCooldown.durationMs = 5000L;
                if ((com.adjustclient.core.changelog.Changelog.versionCode >>> 16) <= (vendorRemoteVersion.id & 0xFFFF)) {
                    com.adjustclient.core.gui.hud.notification.NotificationBuilder notice =
                            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Update Notice")
                                    .addText("You are running an outdated version of Adjust.", (int) 4290690237L);
                    notice.duration = 1_000_000L;
                    notice.lifetimeSteps = 100_000;
                    notice.key = "adjust.updatenotice";
                    com.adjustclient.core.gui.hud.notification.NotificationAction restartAction =
                            new com.adjustclient.core.gui.hud.notification.NotificationAction();
                    restartAction.label = "Restart";
                    restartAction.event = vendorRestart;
                    notice.actions.add(restartAction);
                    com.adjustclient.core.gui.hud.notification.NotificationAction ignoreAction =
                            new com.adjustclient.core.gui.hud.notification.NotificationAction();
                    ignoreAction.label = "Ignore";
                    ignoreAction.event = vendorIgnoreUpdate;
                    notice.actions.add(ignoreAction);
                    notice.show();
                }
            }
        }
        Minecraft mc = Minecraft.getMinecraft();
        boolean inMenu = mc.currentScreen instanceof AdjustMainMenu
                || mc.currentScreen instanceof com.adjustclient.core.changelog.ChangelogScreen;
        boolean previous = menuContext;
        if (inMenu != previous) {
            menuContext = inMenu;
            new com.adjustclient.utilities.backend.api.request.polls.GetPollRequest(inMenu).toPacket()
                    .sendAsync(response -> {
                        if (response.status != com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) menuContext = previous;
                    });
        }
        if (mc.thePlayer == null && mc.theWorld == null && pendingSessionChange != null) {
            pendingSessionChange.run();
            pendingSessionChange = null;
        }
        Adjust.INSTANCE.screenStack.responses.expireRequests();
        if (!tagRequestInFlight
                && (tagRequestTimer == null || tagRequestTimer.hasElapsed(2000L))
                && connection.authenticated && connection.ready
                && Adjust.INSTANCE.tagManager != null) {
            tagRequestInFlight = true;
            tagRequestTimer = new Timer();
            new com.adjustclient.utilities.backend.api.request.client.RecoverAccountRequest().toPacket().sendAsync(vendorTagReply());
        }
    }

    @SuppressWarnings("unused")
    private static void vendorResetServiceState() {
        connectionStatusOverlay = new com.adjustclient.core.gui.connection.ConnectionStatusOverlay();
        var accounts = Adjust.INSTANCE.accountManager;
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> accounts.selection.clear());
        if (accounts.searchField != null
                && !accounts.searchField.getText().isEmpty()) accounts.searchChanged = true;
        accounts.selectedAccount = null;
        if (accounts.viewButton != null) {
            accounts.viewButton.enabled = false;
            accounts.loginButton.enabled = false;
        }
        if (accounts.currentRepository != null) {
            accounts.currentRepository = null;
            accounts.selection.clear();
        }
        accounts.repositories.clear();
        ClientInfoCollector.entries.clear();
        if (Adjust.chatManager != null && Adjust.chatManager.globalChannel != null) {
            var chat = Adjust.chatManager.globalChannel;
            chat.pendingMessages = null;
            chat.locallySubscribed = false;
            chat.field48 = null;
        }
        for (com.adjustclient.managers.BackendMessageHandler service : Adjust.INSTANCE.screenStack.field52) {
            try {
                service.onDisconnect();
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(-666);
            }
        }
        accountStatePending = false;
        recoveryRequested = false;
        ahmedElSisiMode = false;
        CreditsRenderer.INSTANCE = null;
    }
}
