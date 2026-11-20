package com.adjustclient.modules.impl.player.voicechat.client.user;

public class TimestampedValue {
    public String reason;
    public String mutedByName;
    public String unmutedByName;
    public String mainUsername;
    public long timeEnd;

    public TimestampedValue(long timeStart, long timeEnd, String reason, String mutedByName,
                            String unmutedByName, String mainUsername) {
        this.timeEnd = timeEnd;
        this.reason = reason;
        this.mutedByName = mutedByName;
        this.unmutedByName = unmutedByName;
        this.mainUsername = mainUsername;
    }

    public static TimestampedValue create(long timeEnd) {
        return new TimestampedValue(System.currentTimeMillis(), timeEnd, null, null, null, null);
    }
    public static TimestampedValue create(long timeEnd, String reason) {
        return new TimestampedValue(System.currentTimeMillis(), timeEnd, reason, null, null, null);
    }
}
