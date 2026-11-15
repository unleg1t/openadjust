package com.adjustclient.module.impl.player.voicechat.channel;

import com.adjustclient.event.EventBus;
import com.adjustclient.event.Listener;
import com.adjustclient.event.VoicePlayerLeaveEvent;
import com.adjustclient.event.VoiceVolumeEvent;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.module.impl.player.voicechat.client.VoiceSessionManager;
import com.adjustclient.module.impl.player.voicechat.client.VoiceSender;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound.ServerboundErrorReportPacket;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperty;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class VoiceChatController implements Listener {
    public static VoiceChatController INSTANCE;
    public final ChannelRegistry channelRegistry;
    public UUID currentChannelId;
    public boolean field12;
    public boolean field13;
    public boolean currentChannelProximity;

    public VoiceChatController() {
        channelRegistry = new ChannelRegistry();
        currentChannelId = new UUID(0L, 0L);
    }

    public void reset() {
        System.currentTimeMillis();
        channelRegistry.clear();
    }

    public void reportError(boolean fatal, String message) {
        VoiceSender sender = findAuthenticatedSender();
        if (sender == null) return;
        ServerboundErrorReportPacket packet = new ServerboundErrorReportPacket(message, fatal);
        try {
            sender.client.udpClient.sendPacket(
                    packet, sender.client.udpClient.connection, null);
        } catch (Exception ignored) {
        }
    }

    public void updateUser(UUID userId, UserProperties properties) {
        Channel channel = channelRegistry.getUserChannel(userId);
        if (channel == null) return;
        VoiceUser user = channel.users.get(userId);
        if (user == null) return;
        for (Map.Entry<com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperty, Object> entry : properties.properties.entrySet()) {
            user.properties.set(entry.getKey(), entry.getValue());
        }
        VoiceVolumeEvent event = new VoiceVolumeEvent(user);
        EventBus.post(event);
    }

    public void removeUserFromChannel(UUID userId, UUID channelId) {
        Channel channel = channelRegistry.channels.get(channelId);
        if (channel == null) {
            Channel actual = channelRegistry.getUserChannel(userId);
            reportError(true, "Can't remove user with id " + userId + " from channel with id " + channelId
                    + " because the channel doesn't exist (actual channel: " + (actual == null ? "null" : "'" + actual.getName() + "'") + ")");
            return;
        }
        try {
            VoiceUser user = channel.users.get(userId);
            if (user == null) {
                Channel actual = channelRegistry.getUserChannel(userId);
                reportError(true, "Can't remove user with id " + userId + " from channel " + channel.getName()
                        + " because the user doesn't exist in this channel (user is in channel: "
                        + (actual == null ? "null" : "'" + actual.getName() + "'") + ")");
                return;
            }
            if (!isCurrentUser(userId) && currentChannelId.equals(channel.id)) channel.isCustom();
            channelRegistry.removeUser(channel.id, user.id);
            VoicePlayerLeaveEvent event = new VoicePlayerLeaveEvent(user.id, channel);
            EventBus.post(event);
        } catch (RuntimeException exception) {
            reportError(true, "Can't remove user with id " + userId + " from channel " + channel.getName()
                    + " because of an error: " + exception.getMessage());
        }
    }

    public void addUserToChannel(UUID userId, UUID channelId, UserProperties properties) {
        Channel channel = channelRegistry.channels.get(channelId);
        if (channel == null) {
            reportError(true, "Can't add user with id " + userId + " to channel with id " + channelId + " because it doesn't exist");
            return;
        }
        try {
            Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
            if (session == null) session = Minecraft.getMinecraft().getSession();
            if (userId.equals(session.getProfile().getId())) {
                if (!currentChannelId.equals(channelId)) currentChannelId = channelId;
            } else if (currentChannelId.equals(channel.id)) {
                channel.isCustom();
            }
            channelRegistry.addUser(channel, new VoiceUser(userId, properties));
        } catch (RuntimeException exception) {
            reportError(true, "Can't add user with id " + userId + " to channel " + channel.getName()
                    + " because of an error: " + exception.getMessage());
        }
    }

    public void moveUser(UUID userId, UUID fromChannelId, UUID toChannelId, UserProperties properties) {
        removeUserFromChannel(userId, fromChannelId);
        addUserToChannel(userId, toChannelId, properties);
        Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
        if (session == null) session = Minecraft.getMinecraft().getSession();
        if (userId.equals(session.getProfile().getId())) {
            boolean proximity = isCurrentChannelProximity();
            if (currentChannelProximity != proximity) {
                currentChannelProximity = proximity;
                VoiceChatModule.INSTANCE.getAudioStreamManager().clear();
            }
        }
    }

    public boolean isCurrentUser(UUID userId) {
        Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
        return session != null && userId.equals(session.getProfile().getId());
    }

    public Channel getCurrentChannel() { return channelRegistry.channels.get(currentChannelId); }

    public boolean hasActiveChannel() {
        Channel channel = getCurrentChannel();
        return channel != null && (channel.id.getMostSignificantBits() != 0L || channel.id.getLeastSignificantBits() != 0L);
    }

    public boolean isCurrentChannelProximity() {
        Channel channel = getCurrentChannel();
        return channel == null || (Boolean) channel.properties.properties.getOrDefault(ChannelProperty.PROXIMITY, Boolean.FALSE);
    }

    public static VoiceSender findAuthenticatedSender() {
        VoiceSessionManager manager = VoiceChatModule.INSTANCE.getSessionManager();
        if (manager.client.udpClient == null
                || !manager.client.udpClient.open.get()
                || !manager.handler.authenticated) return null;
        return manager.sender;
    }

    public ChannelRegistry getChannelRegistry() { return channelRegistry; }
}
