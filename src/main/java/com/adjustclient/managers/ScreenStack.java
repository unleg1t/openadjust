package com.adjustclient.managers;

import com.adjustclient.core.events.Listener;
import com.adjustclient.managers.config.ConfigurationService;
import com.adjustclient.managers.message.DirectMessageService;
import com.adjustclient.managers.party.PartyService;
import com.adjustclient.managers.prefs.SyncedPreferenceService;
import com.adjustclient.managers.storage.LocalStorage;
import com.adjustclient.core.gui.friends.FriendsScreen;
import com.adjustclient.core.gui.screen.AdjustScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;

public final class ScreenStack implements Listener {
    public static boolean deferOverlayChanges;
    public com.adjustclient.managers.servertracker.KnownPlayers knownPlayers;
    public PartyService partyService;
    public ConfigurationService configurations;
    public com.adjustclient.managers.servertracker.FriendServerTracker serverTracker;
    public Object field20;
    public com.adjustclient.core.settings.property.Manager<com.adjustclient.managers.friends.Friend> friends;
    public com.adjustclient.managers.friends.BlockListService blockList;
    public SyncedPreferenceService preferences;
    public com.adjustclient.managers.payload.PayloadService payloads;
    public Object field40;
    public com.adjustclient.utilities.backend.api.ResponseRouter responses;
    public DirectMessageService directMessages;
    public BackendMessageHandler[] field52;
    public List<AdjustScreen> overlays = new ArrayList<>();
    public com.adjustclient.managers.poll.PollService poll;
    public com.adjustclient.managers.config.user.UserConfiguration localStorage;
    public FriendsScreen friendsScreen;
    public Object field72;

    public ScreenStack() {
        knownPlayers = new com.adjustclient.managers.servertracker.KnownPlayers();
        partyService = new PartyService();
        configurations = new ConfigurationService();
        serverTracker = new com.adjustclient.managers.servertracker.FriendServerTracker();
        var overlay = new com.adjustclient.managers.party.PartyOverlay(partyService);
        com.adjustclient.managers.party.PartyOverlay.INSTANCE = overlay;
        field20 = overlay;
        friends = new com.adjustclient.core.settings.property.Manager<>() {
            @Override
            protected boolean matches(com.adjustclient.managers.friends.Friend friend, Object key) {
                if (key instanceof com.adjustclient.managers.user.ClientUser) return friend.user == key;
                if (key instanceof String) return friend.user != null && friend.user.name.equalsIgnoreCase((String) key);
                return friend == key;
            }
        };
        blockList = new com.adjustclient.managers.friends.BlockListService();
        com.adjustclient.managers.friends.BlockListService.INSTANCE = blockList;
        preferences = new SyncedPreferenceService();
        payloads = new com.adjustclient.managers.payload.PayloadService();
        directMessages = new DirectMessageService();
        field52 = new BackendMessageHandler[] {
                partyService, configurations, serverTracker, blockList,
                preferences, payloads, directMessages,
                new com.adjustclient.managers.misc.CollectionSyncHandler()
        };
        localStorage = com.adjustclient.managers.config.user.UserConfiguration.INSTANCE;
    }

    @SuppressWarnings("unused")
    private void vendorInitResponses() {
        responses = new com.adjustclient.utilities.backend.api.ResponseRouter();
        field52 = new BackendMessageHandler[] {
                partyService, configurations, serverTracker, blockList,
                preferences, payloads, responses, directMessages,
                new com.adjustclient.managers.misc.CollectionSyncHandler()
        };
    }

    public void openFriendsScreen() {
        if (!AdjustMainMenu.clientUiAllowed) return;
        if (friendsScreen == null) friendsScreen = new FriendsScreen();
        FriendsScreen screen = friendsScreen;
        com.adjustclient.managers.util.ServiceUtil.closeOverlay(deferOverlayChanges, () -> {
            // The scheduled body is the same push operation, including both lifecycle hooks,
            // duplicate suppression and the blocking-overlay focus change.
            openOverlay(screen);
        });
    }

    public void closeOverlay(AdjustScreen screen) {
        if (overlays.remove(screen)) screen.onClose();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) KeyBinding.unPressAllKeys();
        if (hasBlockingOverlay()) mc.setIngameNotInFocus();
        else if (mc.currentScreen == null) mc.setIngameFocus();
    }

    public void openOverlay(AdjustScreen screen) {
        if (overlays.contains(screen)) return;
        if (screen instanceof com.adjustclient.core.gui.config.GlobalConfigsScreen) {
            ((com.adjustclient.core.gui.config.GlobalConfigsScreen) screen).refreshIfStale();
        } else {
            screen.onAdded();
        }
        screen.onOpen();
        overlays.add(screen);
        if (screen.blocksGameInteraction) Minecraft.getMinecraft().setIngameNotInFocus();
    }

    public void addOverlay(AdjustScreen screen) {
        com.adjustclient.managers.util.ServiceUtil.closeOverlay(deferOverlayChanges, () -> openOverlay(screen));
    }

    public void removeOverlay(AdjustScreen screen) {
        com.adjustclient.managers.util.ServiceUtil.closeOverlay(true, () -> closeOverlay(screen));
    }

    public void renderOverlays(com.adjustclient.core.events.HudMouseRenderEvent event) {
        boolean prevDefer = deferOverlayChanges;
        deferOverlayChanges = true;
        try {
            AdjustScreen top = getTopOverlay();
            for (AdjustScreen screen : overlays) {
                com.adjustclient.utilities.Vec2f mouse = screen == top
                        ? new com.adjustclient.utilities.Vec2f(event.getMouseX(), event.mouseY)
                        : new com.adjustclient.utilities.Vec2f(-111.0F, -111.0F);
                screen.render(event, mouse, event.scaledResolution);
            }
        } finally {
            deferOverlayChanges = prevDefer;
        }
    }

    public void tickOverlays() {
        boolean prevDefer = deferOverlayChanges;
        deferOverlayChanges = true;
        try {
            for (int i = overlays.size() - 1; i >= 0; i--) {
                AdjustScreen screen = overlays.get(i);
                if (screen.blocksGameInteraction) {
                    screen.tickInput();
                    screen.updateScreen();
                    break;
                }
            }
        } finally {
            deferOverlayChanges = prevDefer;
        }
    }

    public boolean hasBlockingOverlay() {
        return overlays.stream().anyMatch(screen -> screen.blocksGameInteraction);
    }

    public AdjustScreen getTopOverlay() {
        return overlays.isEmpty() ? null : overlays.get(overlays.size() - 1);
    }
}
