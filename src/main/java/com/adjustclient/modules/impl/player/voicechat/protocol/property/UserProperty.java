package com.adjustclient.modules.impl.player.voicechat.protocol.property;

public enum UserProperty implements PropertyKey {
    NAME(String.class),
    COUNTRY_CODE(String.class),
    INPUT_MUTED(Boolean.class),
    OUTPUT_MUTED(Boolean.class),
    INPUT_DISABLED(Boolean.class),
    OUTPUT_DISABLED(Boolean.class);

    private final String key;
    private final Class<?> type;

    UserProperty(Class<?> type) {
        this.key = createKey();
        this.type = type;
    }
    public String getKey() { return key; }
    public Class<?> getType() { return type; }
}
