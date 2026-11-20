package com.adjustclient.modules.impl.player.voicechat.channel.model;

import com.adjustclient.modules.impl.player.voicechat.channel.VoiceUser;
import com.adjustclient.modules.impl.player.voicechat.channel.registry.Identifiable;
import com.adjustclient.modules.impl.player.voicechat.channel.registry.IdentifiableRegistry;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.ChannelProperties;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.UserProperty;
import java.util.Comparator;
import java.util.UUID;

public abstract class Channel implements Identifiable {
    public final UUID id;
    public final IdentifiableRegistry<VoiceUser> users;
    public ChannelProperties properties;

    protected Channel(UUID id) {
        this.id = id;
        users = new IdentifiableRegistry<>(Comparator.comparing(
            (VoiceUser user) -> ((String) user.properties.properties.getOrDefault(
                (UserProperty) UserProperty.NAME, "<unknown>")).toLowerCase()));
        properties = new ChannelProperties();
    }

    @Override public UUID getId() { return id; }

    public IdentifiableRegistry<VoiceUser> getUsers() { return users; }
    public ChannelProperties getProperties() { return properties; }
    public abstract String getName();
    public abstract boolean isOwner();

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return id.equals(((Channel) other).id);
    }

    public boolean isCustom() { return true; }

    @Override public int hashCode() { return id.hashCode(); }
}
