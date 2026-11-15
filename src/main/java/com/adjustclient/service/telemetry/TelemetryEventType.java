package com.adjustclient.service.telemetry;

public enum TelemetryEventType {
    CHEATING("Cheating through the "),
    SECURITYALERT("Suspicious activity"),
    CROSSTEAMING("Cross teaming"),
    ENCOURAGING("encourages cheating"),
    GRIEFING("negatively affecting your fellow team members"),
    BOOSTING("Boosting "),
    OTHER(null);

    public final String keyword;

    TelemetryEventType(String keyword) {
        this.keyword = keyword;
    }

    public static TelemetryEventType match(String message) {
        if (message == null) {
            return OTHER;
        }
        for (TelemetryEventType type : values()) {
            if (type.keyword != null && message.contains(type.keyword)) {
                return type;
            }
        }
        return OTHER;
    }
}
