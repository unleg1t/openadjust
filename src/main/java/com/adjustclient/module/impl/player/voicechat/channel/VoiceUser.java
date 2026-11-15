package com.adjustclient.module.impl.player.voicechat.channel;

import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.channel.registry.Identifiable;
import com.adjustclient.module.impl.player.voicechat.playback.VoicePlaybackState;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;
import java.util.UUID;

public final class VoiceUser implements Identifiable {
    public final UUID id;
    public final UserProperties properties;

    public VoiceUser(UUID id, UserProperties properties) {
        this.id = id;
        this.properties = properties;
    }

    @Override public UUID getId() { return id; }

    public VoicePlaybackState getAudioStream() {
        return VoiceChatModule.INSTANCE.getAudioStreamManager().getPlaybackState(id, true);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return id.equals(((VoiceUser) other).id);
    }

    @Override public int hashCode() { return id.hashCode(); }
}
