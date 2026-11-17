package com.adjustclient;

import com.adjustclient.Adjust;
import com.adjustclient.backend.transport.HydraConnection;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.Listener;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.SettingContainer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import com.adjustclient.game.entity.EntityTrackerService;
import com.adjustclient.game.mineplex.MineplexGameTracker;
import com.adjustclient.game.rewound.RewoundDisablerTracker;
import com.adjustclient.game.rewound.RewoundGame;
import com.adjustclient.game.rewound.RewoundGameTracker;
import com.adjustclient.game.movable.MovableManager;
import com.adjustclient.game.packet.InboundPacketQueue;
import com.adjustclient.game.packet.OutboundPacketQueue;
import com.adjustclient.game.player.ChatMessageSender;
import com.adjustclient.game.player.ChatTextReplacer;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.game.player.LookTargetHandler;
import com.adjustclient.game.player.SlotSpoofer;
import com.adjustclient.game.render.FakeTabListManager;
import com.adjustclient.game.render.FrozenPlayerRenderListener;
import com.adjustclient.game.render.PositionSnapper;
import com.adjustclient.game.render.progress.ProgressBarRenderer;
import com.adjustclient.hypixel.HypixelGameEventReporter;
import com.adjustclient.hypixel.HypixelGameTracker;
import com.adjustclient.hypixel.HypixelPacketBalance;
import com.adjustclient.hypixel.HypixelPlacementLimiter;
import com.adjustclient.hypixel.TabListTracker;
import com.adjustclient.module.util.CpsCounter;
import com.adjustclient.property.CommandManager;
import com.adjustclient.property.KeybindManager;
import com.adjustclient.property.ModuleManager;
import com.adjustclient.property.TagManager;
import com.adjustclient.service.ScreenStack;
import com.adjustclient.service.session.SessionService;
import com.adjustclient.service.telemetry.TelemetryListener;
import com.adjustclient.ui.account.AccountManagerScreen;
import com.adjustclient.ui.chat.ChatChannelManager;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.prediction.MotionPredictor;
import com.adjustclient.vfs.VirtualFileSystem;
import java.util.concurrent.atomic.AtomicBoolean;
import com.adjustclient.game.mineplex.MineplexGame;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.config.ConfigurationService;

public final class Adjust implements Listener {
    public static String NAME;
    public static HydraConnection hydraConnection;
    public static String username;
    public static Adjust INSTANCE;
    public static MotionPredictor movementPredictor;
    public static ChatChannelManager chatManager;
    public static InventoryStateTracker inventoryStateTracker;
    public static LookTargetHandler lookTargetHandler;
    public static TelemetryListener telemetryListener;
    public static MovableManager movableManager;
    public static CpsCounter cpsCounter;
    public static HypixelPacketBalance packetBalance;
    public static HypixelGameTracker gameStateTracker;
    public static MineplexGameTracker mineplexGameTracker;
    public static RewoundGameTracker rewoundGameTracker;
    public static RewoundDisablerTracker rewoundDisablerTracker;
    public static OutboundPacketQueue outboundPacketQueue;
    public static InboundPacketQueue inboundPacketQueue;
    public static SlotSpoofer slotSpoofer;
    public static ChatMessageSender chatMessageSender;
    public static ChatTextReplacer chatTextReplacer;
    public static FakeTabListManager fakeTabListManager;
    public static HypixelGameEventReporter gameEventReporter;
    public static PositionSnapper positionSnapper;
    public static ProgressBarRenderer progressBarRenderer;
    public static EntityTrackerService entityTrackerService;
    public static HypixelPlacementLimiter placementLimiter;
    public static TabListTracker tabListTracker;
    public static FrozenPlayerRenderListener frozenPlayerRenderListener;
    public static com.adjustclient.module.util.MouseClickHelper mouseClickHelper;
    public static com.adjustclient.hypixel.api.HypixelApiManager hypixelApiManager;
    public static com.adjustclient.util.build.ValueHolder valueHolder;
    public static com.adjustclient.util.latency.LatencyTracker latencyTracker;
    public static com.adjustclient.util.placement.PlacementVerifier placementVerifier;
    public static byte serviceInitializationStage;
    public ModuleManager moduleManager;
    public CommandManager commandManager;
    public KeybindManager bindManager;
    public VirtualFileSystem fileSystem;
    public AccountManagerScreen accountManager;
    public ScreenStack screenStack;
    public Object field28;
    public TagManager tagManager;
    public Object field36;
    public com.adjustclient.property.UserManager field40;
    public SessionService sessionService;
    public Object field48;

    public Adjust() {
        INSTANCE = this;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        String sessionUsername = mc != null && mc.getSession() != null ? mc.getSession().getUsername() : null;
        username = (sessionUsername != null && !sessionUsername.isEmpty()) ? sessionUsername : "Offline";

        com.adjustclient.service.config.user.UserConfiguration.INSTANCE =
                new com.adjustclient.service.config.user.UserConfiguration();
        this.sessionService = new SessionService();
        this.tagManager = new TagManager();
        this.bindManager = new KeybindManager();
        this.fileSystem = new VirtualFileSystem();
        VirtualFileSystem.INSTANCE = this.fileSystem;
        initServices();
        this.field28 = new com.adjustclient.property.GlobalPropertiesModule();
        this.screenStack = new ScreenStack();
        if (com.adjustclient.service.party.PartyOverlay.INSTANCE != null) {
            com.adjustclient.service.party.PartyOverlay.INSTANCE.initialize();
        }
        this.moduleManager = new ModuleManager();
        this.commandManager = new CommandManager();
        this.accountManager = new AccountManagerScreen();
        com.adjustclient.ui.account.LocalAccountStore.INSTANCE.open(this.accountManager);
        ServerTracker.clientInstance = this;
        this.field40 = new com.adjustclient.property.UserManager();
        this.field40.field16 = this.field40.getOrCreate(username);
        com.adjustclient.script.engine.ScriptManager.INSTANCE = new com.adjustclient.script.engine.ScriptManager();
        com.adjustclient.module.impl.render.hud.HUDModule.V();
        if (com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE != null) {
            com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE.initialize();
        }
        com.adjustclient.service.util.ServiceUtil.runPendingTasks();
        registerAllEventListeners();
    }

    public void registerAllEventListeners() {
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        visitAndRegisterListener(this, visited);
        visitAndRegisterListener(this.moduleManager, visited);
        visitAndRegisterListener(this.commandManager, visited);
        visitAndRegisterListener(this.bindManager, visited);
        visitAndRegisterListener(this.fileSystem, visited);
        visitAndRegisterListener(this.accountManager, visited);
        visitAndRegisterListener(this.screenStack, visited);
        visitAndRegisterListener(this.field28, visited);
        visitAndRegisterListener(this.tagManager, visited);
        visitAndRegisterListener(this.field36, visited);
        visitAndRegisterListener(this.field40, visited);
        visitAndRegisterListener(this.sessionService, visited);
        visitAndRegisterListener(this.field48, visited);
        if (this.commandManager != null) {
            visitAndRegisterListener(this.commandManager.view, visited);
        }
        if (this.moduleManager != null) {
            visitAndRegisterListener(this.moduleManager.view, visited);
        }

        visitAndRegisterListener(movementPredictor, visited);
        visitAndRegisterListener(chatManager, visited);
        visitAndRegisterListener(inventoryStateTracker, visited);
        visitAndRegisterListener(lookTargetHandler, visited);
        visitAndRegisterListener(movableManager, visited);
        visitAndRegisterListener(cpsCounter, visited);
        visitAndRegisterListener(packetBalance, visited);
        visitAndRegisterListener(gameStateTracker, visited);
        visitAndRegisterListener(mineplexGameTracker, visited);
        visitAndRegisterListener(rewoundGameTracker, visited);
        visitAndRegisterListener(rewoundDisablerTracker, visited);
        visitAndRegisterListener(outboundPacketQueue, visited);
        visitAndRegisterListener(inboundPacketQueue, visited);
        visitAndRegisterListener(slotSpoofer, visited);
        visitAndRegisterListener(chatMessageSender, visited);
        visitAndRegisterListener(chatTextReplacer, visited);
        visitAndRegisterListener(fakeTabListManager, visited);
        visitAndRegisterListener(positionSnapper, visited);
        visitAndRegisterListener(progressBarRenderer, visited);
        visitAndRegisterListener(entityTrackerService, visited);
        visitAndRegisterListener(placementLimiter, visited);
        visitAndRegisterListener(tabListTracker, visited);
        visitAndRegisterListener(frozenPlayerRenderListener, visited);
        visitAndRegisterListener(mouseClickHelper, visited);
        visitAndRegisterListener(hypixelApiManager, visited);
        visitAndRegisterListener(valueHolder, visited);
        visitAndRegisterListener(latencyTracker, visited);
        visitAndRegisterListener(placementVerifier, visited);

        visitAndRegisterListener(ServerTracker.INSTANCE, visited);
        if (ServerTracker.INSTANCE != null) {
            visitAndRegisterListener(ServerTracker.INSTANCE.field56, visited);
        }
        visitAndRegisterListener(com.adjustclient.game.entity.tracker.ServerPositionTracker.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.rotation.handler.RotationHandler.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.rotation.handler.RenderRotationTracker.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.service.party.PartyOverlay.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.inventory.quickbuy.QuickBuyListener.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.exploit.murdermystery.MurderMysteryAutoStrike.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.combat.builder.BuilderPlacer.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.combat.builder.feature.headhitter.HeadHitterFeature.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseRecorder.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.script.engine.ScriptManager.INSTANCE, visited);
        if (com.adjustclient.script.engine.ScriptManager.INSTANCE != null) {
            visitAndRegisterListener(com.adjustclient.script.engine.ScriptManager.INSTANCE.bridge, visited);
        }
        visitAndRegisterListener(com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.service.config.user.UserConfiguration.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.ui.account.LocalAccountStore.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.service.storage.LocalStorage.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.service.friends.BlockListService.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.command.impl.debug.DebugCommand.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.AutoGG.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.AutoPlay.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.AutoPoliciesAccept.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.AutoReportAccept.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.AutoTip.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.BanDetect.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.ChatStats.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.bedwars.AntiObsidianMisplace.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTracker.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autorewound.feature.AutoGG.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autorewound.feature.AutoPlay.INSTANCE, visited);
        visitAndRegisterListener(com.adjustclient.module.impl.player.autorewound.feature.PitFeature.INSTANCE, visited);
    }

    private static boolean isExcludedFromEventBus(Class<?> cls) {
        if (cls.isPrimitive() || cls.isEnum() || cls.isAnnotation() || cls.isHidden() || cls.isSynthetic()) {
            return true;
        }
        String name = cls.getName();
        if (name.contains("$$Lambda")) {
            return true;
        }
        if (cls == com.adjustclient.module.impl.movement.scaffold.SafeWalkHelper.class) {
            return true;
        }
        return name.startsWith("com.adjustclient.service.telemetry.")
                || name.equals("com.adjustclient.hypixel.HypixelGameEventReporter")
                || name.startsWith("com.adjustclient.lunar.")
                || name.startsWith("com.adjustclient.backend.")
                || name.startsWith("com.adjustclient.service.servertracker.")
                || name.startsWith("com.adjustclient.ui.connection.")
                || name.startsWith("com.adjustclient.ui.registration.")
                || name.startsWith("com.adjustclient.event.");
    }

    private static void visitAndRegisterListener(Object node, IdentityHashMap<Object, Boolean> visited) {
        if (node == null || visited.put(node, Boolean.TRUE) != null) {
            return;
        }
        Class<?> cls = node.getClass();
        if (isExcludedFromEventBus(cls)) {
            return;
        }
        if (cls.isArray()) {
            if (!cls.getComponentType().isPrimitive()) {
                for (Object element : (Object[]) node) {
                    visitAndRegisterListener(element, visited);
                }
            }
            return;
        }
        if (node instanceof Collection<?> collection) {
            for (Object element : collection) {
                visitAndRegisterListener(element, visited);
            }
            return;
        }
        if (node instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                visitAndRegisterListener(value, visited);
            }
            return;
        }
        String className = cls.getName();
        if (!className.startsWith("com.adjustclient.")) {
            return;
        }
        if (node instanceof Listener listener) {
            EventBus.unregister(listener);
            EventBus.register(listener);
        }
        if (node instanceof SettingContainer container) {
            var settings = container.getSettings();
            if (settings != null) {
                for (var setting : settings) {
                    visitAndRegisterListener(setting, visited);
                }
            }
        }
        if (node instanceof ModeSetting<?> modeSetting) {
            var options = modeSetting.getOptions();
            if (options != null) {
                for (var option : options) {
                    visitAndRegisterListener(option, visited);
                }
            }
        }
        for (Class<?> c = cls; c != null && c != Object.class && c.getName().startsWith("com.adjustclient."); c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    visitAndRegisterListener(field.get(node), visited);
                } catch (RuntimeException | IllegalAccessException ignored) {
                }
            }
        }
    }

    public void shutdown() {
        if (sessionService != null) sessionService.save();
        if (fileSystem != null && fileSystem.dirtyTimer != null
                && ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived) {
            fileSystem.save(new AtomicBoolean(), new Runnable[0]);
        }
        if (screenStack != null) {
            if (screenStack.localStorage != null) {
                screenStack.localStorage.save(new AtomicBoolean());
            }
        }
        if (tagManager != null) tagManager.save();
        if (ServerTracker.INSTANCE != null && ServerTracker.INSTANCE.logWriter != null) ServerTracker.INSTANCE.logWriter.close();
    }

    public static void initServices() {
        NAME = "Adjust";
        movementPredictor = new MotionPredictor();
        var karmaApi = new com.adjustclient.hypixel.api.client.karma.TwentyFiveKarmaApiClient();
        com.adjustclient.hypixel.api.HypixelApiManager api = new com.adjustclient.hypixel.api.HypixelApiManager(karmaApi);
        hypixelApiManager = api;
        hypixelApiManager = api;
        movableManager = new MovableManager();
        cpsCounter = new CpsCounter();
        packetBalance = new HypixelPacketBalance();
        var hypixelState = com.adjustclient.hypixel.HypixelGame.UNKNOWN_GAME.stateFactory.create();
        if (ServerTracker.INSTANCE != null) {
            com.adjustclient.module.Module module = KillAuraModule.INSTANCE;
            if (module != null && module.isEnabled()
                    && hypixelState.game == com.adjustclient.hypixel.HypixelGame.MURDER_MYSTERY) {
                module.setEnabled(false, true);
            }
        }
        gameStateTracker = new HypixelGameTracker(com.adjustclient.server.ServerType.HYPIXEL, hypixelState);
        var mineplexState = MineplexGame.UNKNOWN_GAME.stateFactory.create();
        mineplexGameTracker = new MineplexGameTracker(ServerType.MINEPLEX, mineplexState);
        var rewoundState = RewoundGame.UNKNOWN_GAME.stateFactory.create();
        rewoundGameTracker = new RewoundGameTracker(ServerType.REWOUND, rewoundState);
        rewoundDisablerTracker = new RewoundDisablerTracker();
        serviceInitializationStage = (byte) 5;
        mouseClickHelper = new com.adjustclient.module.util.MouseClickHelper(net.minecraft.client.Minecraft.getMinecraft());
        outboundPacketQueue = new OutboundPacketQueue();
        inboundPacketQueue = new InboundPacketQueue();
        slotSpoofer = new SlotSpoofer();
        chatManager = new ChatChannelManager();
        lookTargetHandler = new LookTargetHandler();
        chatMessageSender = new ChatMessageSender();
        chatTextReplacer = new ChatTextReplacer();
        inventoryStateTracker = new InventoryStateTracker();
        fakeTabListManager = new FakeTabListManager();
        positionSnapper = new PositionSnapper();
        progressBarRenderer = new ProgressBarRenderer();
        com.adjustclient.game.entity.tracker.ServerPositionTracker.INSTANCE = new com.adjustclient.game.entity.tracker.ServerPositionTracker();
        entityTrackerService = new EntityTrackerService(com.adjustclient.game.entity.tracker.ServerPositionTracker.INSTANCE);
        valueHolder = new com.adjustclient.util.build.ValueHolder();
        placementLimiter = new HypixelPlacementLimiter();
        tabListTracker = new TabListTracker();
        latencyTracker = new com.adjustclient.util.latency.LatencyTracker();
        try {
            com.adjustclient.lunar.LunarClientService.initialize();
        } catch (java.io.IOException e) {
            Logger.log(LogLevel.WARN, true, "Failed to initialize Lunar Client service: {}", e);
        }
        placementVerifier = new com.adjustclient.util.placement.PlacementVerifier();
        frozenPlayerRenderListener = new FrozenPlayerRenderListener();
    }

    @SuppressWarnings("unused")
    private void vendorShutdownTelemetry() {
    }

    @SuppressWarnings("unused")
    private static void vendorInitTelemetryServices() {
    }

    @SuppressWarnings("unused")
    private static void vendorInitGameEventReporter(IdentityHashMap<Object, Boolean> visited) {
        gameEventReporter = new HypixelGameEventReporter();
        visitAndRegisterListener(gameEventReporter, visited);
    }
}
