package com.adjustclient.module.impl.player.voicechat;

import com.adjustclient.Adjust;
import com.adjustclient.audio.device.DeviceInfo;
import com.adjustclient.audio.device.DeviceType;
import com.adjustclient.event.DisconnectTeardownEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.PreClientTickEvent;
import com.adjustclient.event.ServerConnectionAttemptEvent;
import com.adjustclient.event.ServerJoinCompleteEvent;
import com.adjustclient.event.VoiceAuthenticatedEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.voicechat.audio.VoiceAudioManager;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceUser;
import com.adjustclient.module.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.module.impl.player.voicechat.channel.model.DefaultChannel;
import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.client.VoiceSessionManager;
import com.adjustclient.module.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfoCache;
import com.adjustclient.module.impl.player.voicechat.playback.AudioStreamManager;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound.ServerboundDisconnectPacket;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperty;
import com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.module.impl.player.voicechat.soundboard.Soundboard;
import com.adjustclient.render.font.AccentText;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.ui.dialog.DropdownElement;
import com.adjustclient.ui.hud.notification.Notification;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.ui.hud.notification.Notifications;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;
import java.net.IDN;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.service.ScreenStack;

public final class VoiceChatModule extends Module implements MinecraftInstance {
    public static VoiceChatModule INSTANCE;
    public final VoiceChatSettings settings;
    public VoiceSessionHandler sessionHandler;
    public VoiceSessionManager sessionManager;
    public VoiceUserInfoCache userInfoCache;
    public AudioStreamManager audioStreamManager;
    public VoiceAudioManager audioManager;
    public VoiceChatController controller;
    public Soundboard soundboard;
    public com.adjustclient.module.impl.player.voicechat.hud.VoiceChatHud field64;
    public final AtomicBoolean playersDirty = new AtomicBoolean();
    public final Set<UUID> watchedPlayers = ConcurrentHashMap.newKeySet();
    public int playerUpdateTicks;
    public long updateDeadline;
    private long deviceRetryDeadline;

    public VoiceChatModule() {
        super("Voice Chat", Category.PLAYER);
        INSTANCE = this;
        settings = new VoiceChatSettings(this);
        field64 = new com.adjustclient.module.impl.player.voicechat.hud.VoiceChatHud(this);
    }

    public VoiceUserInfoCache getUserInfoCache() {
        if (userInfoCache == null) userInfoCache = new VoiceUserInfoCache();
        return userInfoCache;
    }
    public VoiceAudioManager getAudioManager() {
        if (audioManager == null) audioManager = new VoiceAudioManager();
        return audioManager;
    }
    public VoiceChatController getController() {
        if (controller == null) {
            controller = new VoiceChatController();
            VoiceChatController.INSTANCE = controller;
        }
        return controller;
    }

    public AudioStreamManager getAudioStreamManager() {
        if (audioStreamManager == null) {
            VoiceSessionManager manager = getSessionManager();
            VoiceUserInfoCache cache = getUserInfoCache();
            audioStreamManager = new AudioStreamManager(manager, cache, getController());
        }
        return audioStreamManager;
    }

    public static ServerAddress getServerAddress() {
        if (ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null) {
            return new ServerAddress(ServerTracker.INSTANCE.serverState.antiCheatModel.serverType.domain, 25565);
        }
        return mc.getCurrentServerData() == null ? new ServerAddress("", 25565)
                : ServerAddress.fromString(mc.getCurrentServerData().serverIP);
    }

    public void updateVoiceServer(boolean currentServer) {
        VoiceSessionManager manager = getSessionManager();
        if (manager.client.udpClient == null
                || !manager.client.udpClient.open.get()
                || !manager.handler.authenticated) return;
        ServerAddress address = currentServer ? getServerAddress() : new ServerAddress("", 25565);
        Logger.log(LogLevel.INFO, true, "Updating voice chat server: {}:{}", IDN.toASCII(address.getIP()), address.getPort());
        getSessionManager().sender.sendServerAddress(address);
        VoiceChatController channels = getController();
        Channel channel = channels.channelRegistry.channels.get(channels.currentChannelId);
        if (channel == null || channel instanceof DefaultChannel || !channel.isOwner()) return;
        channel.properties.set(ChannelProperty.SERVER_ADDRESS, IDN.toASCII(address.getIP()) + ":" + address.getPort());
        getSessionManager().sender.client.sendProperties(channel.id, channel.properties);
    }

    public void updatePlayers() {
        VoiceAudioManager audio = getAudioManager();
        if ((audio.captureDevice == null || audio.outputDevice == null)
                && System.currentTimeMillis() >= deviceRetryDeadline) {
            deviceRetryDeadline = System.currentTimeMillis() + 5000L;
            audio.selectInitialDevices();
        }
        VoiceSessionManager manager = getSessionManager();
        if (manager.handler.authenticated) {
            if (playersDirty.get() || ++playerUpdateTicks > 100) {
                playersDirty.set(false);
                playerUpdateTicks = 0;
                Set<UUID> ids = new HashSet<>();
                if (mc.thePlayer != null && mc.theWorld != null) {
                    for (EntityPlayer player : mc.theWorld.playerEntities) {
                        if (player.getUniqueID() != null) ids.add(player.getUniqueID());
                    }
                }
                VoiceChatController channels = getController();
                if (channels.hasActiveChannel()) {
                    Channel channel = channels.channelRegistry.channels.get(channels.currentChannelId);
                    if (channel != null) {
                        for (VoiceUser user : channel.users.objects) {
                            if (user.id != null) ids.add(user.id);
                        }
                    }
                }
                ids.addAll(watchedPlayers);
                manager.sender.client.requestUserInfo(ids.toArray(new UUID[0]));
            }
        }
        if (manager.client.udpClient != null
                && manager.client.udpClient.open.get()) {
            updateDeadline = System.currentTimeMillis() + 4000L;
        } else if (updateDeadline - System.currentTimeMillis() < 0L) {
            updateDeadline = System.currentTimeMillis() + 4000L;
            getSessionManager().reconnect();
        }
        getAudioStreamManager().update();
    }

    public void reconnect() {
        getSessionManager().switchSession(mc.getSession());
        updateVoiceServer(true);
        playersDirty.set(true);
    }

    public void disconnect() {
        VoiceChatClient client = getSessionManager().client;
        if (client.udpClient != null) {
            try {
                client.udpClient.sendPacket(new ServerboundDisconnectPacket("Disconnected from server"),
                        client.udpClient.connection, null);
            } catch (Exception ignored) {
                // Failed disconnect notification must not prevent closing the connection.
            }
        }
        if (client.udpClient != null) {
            client.udpClient.setState(ConnectionState.DISCONNECT);
        }
        updateVoiceServer(false);
    }

    @EventHandler
    public void onPreTick(PreClientTickEvent event) {
        if (!isEnabled()) return;
        updatePlayers();
    }

    @EventHandler
    public void onServerConnectAttempt(ServerConnectionAttemptEvent event) {
        if (!isEnabled()) return;
        updateVoiceServer(true);
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isEnabled()) return;
        rebuildSoundboardSettings();
    }

    @EventHandler
    public void onDisconnectTeardown(DisconnectTeardownEvent event) {
        if (!isEnabled()) return;
        disconnect();
    }

    @EventHandler
    public void onServerJoinComplete(ServerJoinCompleteEvent event) {
        if (!isEnabled()) return;
        reconnect();
    }

    @EventHandler
    public void onAuthenticated(VoiceAuthenticatedEvent event) {
        if (!isEnabled()) return;
        Logger.log(LogLevel.INFO, true, "Authenticated in VoiceChat: {}", event.session.getProfile().getName());
        playersDirty.set(true);
    }

    public void openDeviceSelection(DeviceType type) {
        String label = switch (type) { case INPUT -> "Microphone"; case OUTPUT -> "Speaker"; };
        getAudioManager().refresh(type);
        List<DeviceInfo> devices = getAudioManager().getDevices(type);
        if (devices.isEmpty()) {
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = getName();
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("No devices were found.", 0xFFBEBCBD).show();
            return;
        }
        DialogBuilder dialog = new DialogBuilder("Select new " + label);
        String[] names = devices.stream().map(device -> device.name)
                .toArray(String[]::new);
        dialog.addDropdown("Device", names);
        dialog.addButton("Select", screen -> selectDevice(devices, type, label, screen),
                screen -> true);
        dialog.addButton("Cancel", null, screen -> true);
        DialogScreen screen = dialog.build(200.0f);
        com.adjustclient.service.util.ServiceUtil.closeOverlay(ScreenStack.deferOverlayChanges,
                () -> Adjust.INSTANCE.screenStack.openOverlay(screen));
    }

    public boolean toggleMute(UUID playerId) {
        if (playerId == null) return false;
        if (watchedPlayers.contains(playerId)) {
            watchedPlayers.remove(playerId);
            return false;
        }
        watchedPlayers.add(playerId);
        return true;
    }

    public void selectDevice(List<DeviceInfo> devices, DeviceType type, String label, DialogScreen screen) {
        DropdownElement dropdown = (DropdownElement) screen.dropdowns.get(0);
        int index = dropdown.options.indexOf(dropdown.selected);
        if (index == -1) return;
        try {
            DeviceInfo device = devices.get(index);
            getAudioManager().selectDevice(type, device.identifier.toString());
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = getName();
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Updated " + label + ": ", 0xFFBEBCBD).append(device.name).show();
        } catch (Exception exception) {
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = getName();
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Couldn't select device: ", 0xFFBEBCBD).append(exception.getMessage()).show();
        }
    }

    @Override
    public void onEnable() {
        deviceRetryDeadline = System.currentTimeMillis() + 5000L;
        getAudioManager().getDevices(DeviceType.INPUT);
        getAudioManager().getDevices(DeviceType.OUTPUT);
        if (getSessionManager().session == null && mc.getSession() != null) {
            getSessionManager().switchSession(mc.getSession());
        }
        updateVoiceServer(true);
    }

    @Override
    public void onDisable() {
        disconnect();
        if (audioStreamManager != null) {
            audioStreamManager.clear();
        }
    }

    @Override
    public String getDescription() { return "Connects you to LabyMod Voice Chat"; }

    public void rebuildSoundboardSettings() {
        if (soundboard == null) soundboard = new Soundboard();
        if (soundboard.currentSound == null) return;
        soundboard.currentSound = null;
        NotificationBuilder builder = new NotificationBuilder();
        builder.title = "Soundboard";
        builder.parts = new ArrayList<>();
        builder.actions = new ArrayList<>();
        builder.addText("Soundboard has been stopped.", 0xFFBEBCBD);
        builder.lifetimeSteps = 43;
        Notification notification = builder.build();
        HUDModule hud = HUDModule.INSTANCE_ALT != null ? HUDModule.INSTANCE_ALT : HUDModule.INSTANCE;
        if (hud == null || !hud.isEnabled() || hud.notifications == null) return;
        Notifications notifications = hud.notifications;
        notifications.add(notification);
    }

    public VoiceSessionHandler getSessionHandler() {
        if (sessionHandler == null) {
            VoiceUserInfoCache cache = getUserInfoCache();
            VoiceChatController channels = getController();
            sessionHandler = new VoiceSessionHandler(this, cache, channels);
        }
        return sessionHandler;
    }

    public VoiceSessionManager getSessionManager() {
        if (sessionManager == null) sessionManager = new VoiceSessionManager(getSessionHandler());
        return sessionManager;
    }
}
