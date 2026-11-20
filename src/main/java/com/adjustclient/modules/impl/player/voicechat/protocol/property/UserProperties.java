package com.adjustclient.modules.impl.player.voicechat.protocol.property;

import java.util.EnumMap;

public final class UserProperties extends PropertyContainer<UserProperty> {
    public UserProperties() { super(UserProperty.class); }
    public UserProperties(EnumMap<UserProperty, Object> values) { super(values); }
    protected UserProperty[] keys() { return UserProperty.values(); }

    @Override public UserProperties clone() {
        return new UserProperties(new EnumMap<>(properties));
    }
    public boolean isInputMuted() { return (Boolean) properties.getOrDefault(UserProperty.INPUT_MUTED, false); }
    public boolean isOutputMuted() { return (Boolean) properties.getOrDefault(UserProperty.OUTPUT_MUTED, false); }
    public boolean isInputDisabled() { return (Boolean) properties.getOrDefault(UserProperty.INPUT_DISABLED, false); }
    public boolean isOutputDisabled() { return (Boolean) properties.getOrDefault(UserProperty.OUTPUT_DISABLED, false); }
}
