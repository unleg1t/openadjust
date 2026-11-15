package com.adjustclient.module.impl.player.voicechat.client.session;

import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfoCache;
import com.adjustclient.module.impl.player.voicechat.playback.AudioStreamManager;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import java.util.UUID;
import net.minecraft.util.ChatComponentText;

public final class VoiceSessionHandler implements VoiceSessionListener {
    public final VoiceChatModule module;
    public final VoiceUserInfoCache users;
    public final VoiceChatController controller;
    public com.adjustclient.module.impl.player.voicechat.client.VoiceSessionManager sessionManager;
    public AudioStreamManager audioStreams;
    public String disconnectReason;
    public boolean authenticated;

    public VoiceSessionHandler(VoiceChatModule module, VoiceUserInfoCache users, VoiceChatController controller) {
        this.module = module;
        this.users = users;
        this.controller = controller;
    }

    @Override public void onAuthenticated(boolean success) {
        if (sessionManager == null) sessionManager = VoiceChatModule.INSTANCE.getSessionManager();
        net.minecraft.util.Session session = sessionManager.session;
        if (session == null) throw new IllegalStateException("Session is null");
        users.getOrCreate(session.getProfile().getId());
        authenticated = true;
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getMinecraft();
        if (minecraft.getCurrentServerData() != null) {
            sessionManager.sender.sendServerAddress(net.minecraft.client.multiplayer.ServerAddress.fromString(minecraft.getCurrentServerData().serverIP));
        }
        VoiceChatController channels = VoiceChatController.INSTANCE;
        if (channels != null && VoiceChatModule.INSTANCE.isEnabled()) {
            channels.currentChannelId = com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo.NO_CHANNEL;
            channels.channelRegistry.clear();
        }
        if (VoiceChatModule.INSTANCE.isEnabled()) {
            com.adjustclient.event.VoiceAuthenticatedEvent event = new com.adjustclient.event.VoiceAuthenticatedEvent();
            event.session = session;
            com.adjustclient.event.EventBus.post(event);
        }
    }

    @Override public void onModeratorWarning(String message) {
        String fallback = com.adjustclient.module.impl.player.voicechat.client.user.MuteReason.fromId(message);
        ChatMessage formatted = module.getFormattedName().format("You have been warned by the LabyMod moderators: {}",
                java.util.Objects.requireNonNullElse(message, fallback));
        ChatUtil.addChatMessage(formatted.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    @Override public void onDisconnect(com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState state, String reason) {
        disconnectReason = state.name() + ": " + reason;
        authenticated = false;
        users.users.clear();
        VoiceChatController channels = VoiceChatController.INSTANCE;
        if (channels != null && VoiceChatModule.INSTANCE.isEnabled()) {
            channels.currentChannelId = com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo.NO_CHANNEL;
            channels.channelRegistry.clear();
        }
    }

    @Override public void onUserInfo(UUID userId, com.google.gson.JsonObject info) {
        users.getOrCreate(userId).read(info);
    }

    @Override public void onAudio(UUID senderId, byte[] data) {
        if (VoiceChatModule.INSTANCE.settings.micTester.isEnabled()) return;
        if (VoiceChatModule.INSTANCE.watchedPlayers.contains(senderId)) return;
        VoiceUserInfo info = users.get(senderId);
        if (info == null || info.isMuted()) return;
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(data);
        long sequence = buffer.getLong();
        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        if (audioStreams == null) audioStreams = VoiceChatModule.INSTANCE.getAudioStreamManager();
        audioStreams.accept(senderId, payload, sequence);
    }
}
