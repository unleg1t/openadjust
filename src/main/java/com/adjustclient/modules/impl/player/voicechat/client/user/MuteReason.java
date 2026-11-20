package com.adjustclient.modules.impl.player.voicechat.client.user;

public enum MuteReason {
    BEHAVIOR("Behaviour"), SOUNDBOARD("Soundboard"), VOLUME("Volume"),
    RACISM("Racism"), HARASSMENT("Harassment"), CUSTOM("Custom");

    MuteReason(String label) { this.label = label; }
    public String label;

    public static String fromId(String id) {
        if (id == null) return "Unknown";
        for (MuteReason reason : values()) {
            if (id.equals(reason.label.toLowerCase())) return reason.label;
        }
        for (MuteReason reason : values()) {
            if (id.equals(reason.label)) return reason.label;
        }
        return id;
    }
}
