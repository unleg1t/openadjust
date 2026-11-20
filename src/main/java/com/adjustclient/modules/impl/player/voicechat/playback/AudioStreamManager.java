package com.adjustclient.modules.impl.player.voicechat.playback;

import com.adjustclient.modules.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.modules.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.modules.impl.player.voicechat.channel.VoiceUser;
import com.adjustclient.modules.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceSessionManager;
import com.adjustclient.modules.impl.player.voicechat.client.user.VoiceUserInfo;
import com.adjustclient.modules.impl.player.voicechat.client.user.VoiceUserInfoCache;
import com.adjustclient.modules.impl.player.voicechat.playback.realistic.RealisticAudioStream;
import com.adjustclient.utilities.MinecraftInstance;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Session;

public final class AudioStreamManager implements MinecraftInstance {
    public final VoiceSessionManager sessionManager;
    public final VoiceUserInfoCache userInfoCache;
    public final VoiceChatController controller;
    public final ConcurrentHashMap<UUID, PlayerAudioStream> streams = new ConcurrentHashMap<>();

    public AudioStreamManager(VoiceSessionManager sessionManager, VoiceUserInfoCache userInfoCache, VoiceChatController controller) {
        this.sessionManager = sessionManager;
        this.userInfoCache = userInfoCache;
        this.controller = controller;
    }

    public PlayerAudioStream getLocalStream() {
        Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
        if (session == null) session = mc.getSession();
        UUID playerUuid = com.adjustclient.managers.session.SessionService.resolveSessionUuid(session);
        if (playerUuid == null) return null;
        return get(playerUuid);
    }

    public void add(PlayerAudioStream stream) {
        stream.initialize();
        streams.put(stream.playerId, stream);
        if (userInfoCache.get(stream.playerId) == null) userInfoCache.getOrCreate(stream.playerId);
    }

    public void remove(UUID playerId) {
        PlayerAudioStream stream = streams.remove(playerId);
        if (stream == null) return;
        stream.close();
        VoiceUserInfo info = userInfoCache.getOrCreate(playerId);
        com.adjustclient.core.events.EventBus.post(new com.adjustclient.core.events.VoiceStreamRemovedEvent(info, stream));
    }

    public VoicePlaybackState getPlaybackState(UUID playerId, boolean detailed) {
        if (sessionManager.client.udpClient == null
                || !sessionManager.client.udpClient.open.get()
                || !sessionManager.handler.authenticated) return VoicePlaybackState.NETWORK_ERROR;
        VoiceUserInfo info = userInfoCache.get(playerId);
        if (info == null) return VoicePlaybackState.NETWORK_ERROR;
        if (info.isSelf() && !VoiceChatModule.INSTANCE.getAudioManager().isMicrophoneReady()) return VoicePlaybackState.INPUT_DISABLED;
        Channel channel = controller.channelRegistry.getUserChannel(playerId);
        if (channel != null) {
            VoiceUser user = channel.users.get(playerId);
            if (user != null) {
                if (user.properties.isOutputDisabled()) return VoicePlaybackState.OUTPUT_DISABLED;
                if (user.properties.isOutputMuted()) return VoicePlaybackState.OUTPUT_MUTED;
                if (user.properties.isInputMuted()) return VoicePlaybackState.INPUT_MUTED;
                if (user.properties.isInputDisabled()) return VoicePlaybackState.INPUT_DISABLED;
            }
        }
        PlayerAudioStream stream = get(playerId);
        if (stream != null && stream.isTalking()) return VoicePlaybackState.TALKING;
        return info.isMuted() ? VoicePlaybackState.INPUT_GLOBAL_MUTED : VoicePlaybackState.NOT_TALKING;
    }

    public RealisticAudioStream create(EntityPlayer entity, VoiceUserInfo info) {
        RealisticAudioStream stream = new RealisticAudioStream(entity, info);
        stream.initialize();
        streams.put(stream.playerId, stream);
        if (userInfoCache.get(stream.playerId) == null) userInfoCache.getOrCreate(stream.playerId);
        return stream;
    }

    public void accept(UUID playerId, byte[] data, long sequence) {
        if (playerId == null || !VoiceChatModule.INSTANCE.isEnabled()) return;
        Channel current = controller.channelRegistry.channels.get(controller.currentChannelId);
        if (current != null && !current.id.equals(new UUID(0L, 0L))) {
            Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
            UUID localId = com.adjustclient.managers.session.SessionService.resolveSessionUuid(session);
            if (localId == null) return;
            VoiceUser localUser = current.users.get(localId);
            if (localUser == null || !localUser.getAudioStream().receiveAllowed) return;
        }
        PlayerAudioStream stream = streams.get(playerId);
        if (stream == null) {
            VoiceUserInfo info = userInfoCache.getOrCreate(playerId);
            if (controller.isCurrentChannelProximity()
                    && !VoiceChatModule.INSTANCE.settings.micTester.getAsBoolean()) {
                if (mc.theWorld == null) return;
                EntityPlayer entity = mc.theWorld.getPlayerEntityByUUID(playerId);
                if (entity == null) return;
                stream = create(entity, info);
            } else {
                stream = new PlayerAudioStream(info);
                add(stream);
            }
        }
        stream.accept(data, sequence);
        stream.receivedBytes += data.length;
        stream.receivedPackets++;
    }

    public void update() {
        if (streams.isEmpty()) return;
        for (Map.Entry<UUID, PlayerAudioStream> entry : streams.entrySet()) {
            PlayerAudioStream stream = entry.getValue();
            stream.update();
            if (stream.isExpired()) remove(entry.getKey());
        }
    }

    public PlayerAudioStream get(UUID playerId) { return playerId == null ? null : streams.get(playerId); }

    public void clear() {
        for (UUID playerId : streams.keySet().toArray(new UUID[0])) remove(playerId);
    }
}
