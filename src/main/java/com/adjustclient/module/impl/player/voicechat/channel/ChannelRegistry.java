package com.adjustclient.module.impl.player.voicechat.channel;

import com.adjustclient.module.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.module.impl.player.voicechat.channel.model.DefaultChannel;
import com.adjustclient.module.impl.player.voicechat.channel.registry.IdentifiableRegistry;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperty;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ChannelRegistry {
    public final IdentifiableRegistry<Channel> channels;
    public final Map<UUID, UUID> userChannels = new HashMap<>();

    public ChannelRegistry() {
        channels = new IdentifiableRegistry<>(Comparator.comparingLong(
            channel -> {
                if (channel instanceof DefaultChannel) {
                    return 0L;
                }
                return (Long) channel.properties.properties.getOrDefault(
                    (ChannelProperty) ChannelProperty.CREATION_TIME, Long.valueOf(0L));
            }));
    }

    public void clear() {
        userChannels.clear();
        channels.objects.clear();
        channels.byId.clear();
    }

    public void addChannel(Channel channel) {
        channels.register(channel);
        for (VoiceUser user : channel.users.objects) userChannels.put(user.id, channel.id);
    }

    public void addUser(Channel channel, VoiceUser user) {
        userChannels.put(user.id, channel.id);
        channel.users.register(user);
    }

    public void removeUser(UUID channelId, UUID userId) {
        if (!Objects.equals(channelId, userChannels.get(userId)) || channelId == null) {
            throw new IllegalArgumentException("User with id " + userId + " not in the channel with id " + channelId);
        }
        Channel channel = channels.get(channelId);
        if (channel == null) throw new IllegalArgumentException("Channel with id " + channelId + " not found");
        channel.users.remove(userId);
        userChannels.remove(userId);
    }

    public void removeChannel(Channel channel) {
        for (VoiceUser user : channel.users.objects) userChannels.remove(user.id);
        channels.remove(channel);
    }

    public Channel getUserChannel(UUID userId) {
        UUID id = userChannels.get(userId);
        return id == null ? null : channels.get(id);
    }

    public void getChannel(UUID channelId) {
        Channel channel = channels.get(channelId);
        if (channel == null) throw new IllegalArgumentException("Channel with id " + channelId + " not found");
        removeChannel(channel);
    }
}
