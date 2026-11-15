package com.adjustclient.module.impl.player.voicechat.protocol.property;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public final class ChannelProperties extends PropertyContainer<ChannelProperty> {
    public ChannelProperties() { super(ChannelProperty.class); }
    public ChannelProperties(EnumMap<ChannelProperty, Object> values) { super(values); }
    protected ChannelProperty[] keys() { return ChannelProperty.values(); }

    @Override protected Object parseValue(Class<?> type, String value) {
        if (type == ChannelVisibility.class) return ChannelVisibility.valueOf(value);
        return super.parseValue(type, value);
    }

    public boolean canTalk(UUID userId) {
        if ((Boolean) properties.getOrDefault(ChannelProperty.TALK_POWER, true)) return true;
        if (((UUID) properties.get(ChannelProperty.OWNER)).equals(userId)) return true;
        if (!properties.containsKey(ChannelProperty.TALKERS)) return false;
        List<?> talkers = (List<?>) properties.get(ChannelProperty.TALKERS);
        return talkers.contains(userId.toString());
    }

    @Override public ChannelProperties clone() {
        return new ChannelProperties(new EnumMap<>(properties));
    }
}
