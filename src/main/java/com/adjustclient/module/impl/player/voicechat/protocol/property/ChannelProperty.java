package com.adjustclient.module.impl.player.voicechat.protocol.property;

import java.util.List;
import java.util.UUID;

public enum ChannelProperty implements PropertyKey {
    OWNER(UUID.class),
    PASSWORD(String.class),
    CREATION_TIME(Long.class),
    SERVER_ADDRESS(String.class),
    VISIBILITY(ChannelVisibility.class),
    INDIRECT_FRIENDS(Boolean.class),
    PROXIMITY(Boolean.class),
    TALK_POWER(Boolean.class),
    TALKERS(List.class),
    BANNED(List.class),
    NAME(String.class);

    private final String key;
    private final Class<?> type;

    ChannelProperty(Class<?> type) {
        this.key = createKey();
        this.type = type;
    }

    public String getKey() { return key; }
    public Class<?> getType() { return type; }
}
