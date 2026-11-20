package com.adjustclient.modules.impl.player.voicechat.client.user;

import java.util.Objects;
import java.util.UUID;

public final class VoiceMute {
    public final String reason;
    public final String reportedBy;
    public final String field12;
    public final UUID tapeId;
    public VoiceMute(int id, String reason, String reportedBy, long reportedAt, String serverAddress, UUID tapeId) {
        this.reason = reason;
        this.reportedBy = reportedBy;
        this.field12 = serverAddress;
        this.tapeId = tapeId;
    }
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof VoiceMute)) return false;
        VoiceMute report = (VoiceMute) other;
        return Objects.equals(reason, report.reason) && Objects.equals(reportedBy, report.reportedBy);
    }
    public int hashCode() { return Objects.hash(reason, reportedBy); }
    public String toString() { return MuteReason.fromId(reason) + " by " + reportedBy; }
}
