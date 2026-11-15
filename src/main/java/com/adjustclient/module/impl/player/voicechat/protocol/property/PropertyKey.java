package com.adjustclient.module.impl.player.voicechat.protocol.property;

public interface PropertyKey {
    String getKey();
    Class<?> getType();

    default String createKey() {
        StringBuilder result = new StringBuilder();
        boolean upper = false;
        for (char c : ((Enum<?>) this).name().toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
        }
        return result.toString();
    }
}
