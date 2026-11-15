package com.adjustclient.service;

import com.adjustclient.event.Listener;
import com.adjustclient.service.config.ConfigurationService;
import com.adjustclient.service.message.DirectMessageService;
import com.adjustclient.service.party.PartyService;
import com.adjustclient.service.prefs.SyncedPreferenceService;
import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.ui.friends.FriendsScreen;
import com.adjustclient.ui.screen.AdjustScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;

public final class ScreenStack implements Listener {
    public static boolean deferOverlayChanges;
    public com.adjustclient.service.servertracker.KnownPlayers knownPlayers;
    public PartyService partyService;
    public ConfigurationService configurations;
    public com.adjustclient.service.servertracker.FriendServerTracker serverTracker;
    public Object field20;
    public com.adjustclient.property.Manager<com.adjustclient.service.friends.Friend> friends;
    public com.adjustclient.service.friends.BlockListService blockList;
    public SyncedPreferenceService preferences;
    public com.adjustclient.service.payload.PayloadService payloads;
    public Object field40;
    public com.adjustclient.backend.api.ResponseRouter responses;
    public DirectMessageService directMessages;
    public BackendMessageHandler[] field52;
    public List<AdjustScreen> overlays = new ArrayList<>();
    public com.adjustclient.service.poll.PollService poll;
    public com.adjustclient.service.config.user.UserConfiguration localStorage;
    public FriendsScreen friendsScreen;
    public Object field72;

    public ScreenStack() {
        knownPlayers = new com.adjustclient.service.servertracker.KnownPlayers();
        partyService = new PartyService();
        configurations = new ConfigurationService();
        serverTracker = new com.adjustclient.service.servertracker.FriendServerTracker();
        var overlay = new com.adjustclient.service.party.PartyOverlay(partyService);
        com.adjustclient.service.party.PartyOverlay.INSTANCE = overlay;
        field20 = overlay;
        friends = new com.adjustclient.property.Manager<>() {
            @Override
            protected boolean matches(com.adjustclient.service.friends.Friend friend, Object key) {
                if (key instanceof com.adjustclient.user.ClientUser) return friend.user == key;
                if (key instanceof String) return friend.user != null && friend.user.name.equalsIgnoreCase((String) key);
                return friend == key;
            }
        };
        blockList = new com.adjustclient.service.friends.BlockListService();
        com.adjustclient.service.friends.BlockListService.INSTANCE = blockList;
        preferences = new SyncedPreferenceService();
        payloads = new com.adjustclient.service.payload.PayloadService();
        directMessages = new DirectMessageService();
        field52 = new BackendMessageHandler[] {
                partyService, configurations, serverTracker, blockList,
                preferences, payloads, directMessages,
                new com.adjustclient.service.misc.CollectionSyncHandler()
        };
        localStorage = com.adjustclient.service.config.user.UserConfiguration.INSTANCE;
    }

    @SuppressWarnings("unused")
    private void vendorInitResponses() {
        responses = new com.adjustclient.backend.api.ResponseRouter();
        field52 = new BackendMessageHandler[] {
                partyService, configurations, serverTracker, blockList,
                preferences, payloads, responses, directMessages,
                new com.adjustclient.service.misc.CollectionSyncHandler()
        };
    }

    public void openFriendsScreen() {
        if (!AdjustMainMenu.clientUiAllowed) return;
        if (friendsScreen == null) friendsScreen = new FriendsScreen();
        FriendsScreen screen = friendsScreen;
        com.adjustclient.service.util.ServiceUtil.closeOverlay(deferOverlayChanges, () -> {
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
        if (screen instanceof com.adjustclient.ui.config.GlobalConfigsScreen) {
            ((com.adjustclient.ui.config.GlobalConfigsScreen) screen).refreshIfStale();
        } else {
            screen.onAdded();
        }
        screen.onOpen();
        overlays.add(screen);
        if (screen.blocksGameInteraction) Minecraft.getMinecraft().setIngameNotInFocus();
    }

    public void addOverlay(AdjustScreen screen) {
        com.adjustclient.service.util.ServiceUtil.closeOverlay(deferOverlayChanges, () -> openOverlay(screen));
    }

    public void removeOverlay(AdjustScreen screen) {
        com.adjustclient.service.util.ServiceUtil.closeOverlay(true, () -> closeOverlay(screen));
    }

    public void renderOverlays(com.adjustclient.event.HudMouseRenderEvent event) {
        boolean prevDefer = deferOverlayChanges;
        deferOverlayChanges = true;
        try {
            AdjustScreen top = getTopOverlay();
            for (AdjustScreen screen : overlays) {
                com.adjustclient.util.Vec2f mouse = screen == top
                        ? new com.adjustclient.util.Vec2f(event.getMouseX(), event.mouseY)
                        : new com.adjustclient.util.Vec2f(-111.0F, -111.0F);
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
