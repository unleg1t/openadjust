package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;

public final class ApolloPingData implements ProtoMessage {
    private final String server;
    private final ApolloPingStatus status;
    private final int ping;
    private final Object field16 = null;

    public ApolloPingData(String server, ApolloPingStatus status, int ping) {
        this.server = server;
        this.status = status;
        this.ping = ping;
    }

    public String getServer() { return server; }
    public ApolloPingStatus getStatus() { return status; }
    public int getPing() { return ping; }

    public static ApolloPingData parse(ProtoReader in) {
        String server = "";
        ApolloPingStatus status = ApolloPingStatus.STATUS_UNSPECIFIED;
        int ping = 0;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> server = in.readString();
                case 16 -> {
                    long value = in.readEnum();
                    status = value == 0 ? ApolloPingStatus.STATUS_UNSPECIFIED
                            : value == 1 ? ApolloPingStatus.STATUS_SUCCESS
                            : value == 2 ? ApolloPingStatus.STATUS_TIMED_OUT
                            : ApolloPingStatus.UNRECOGNIZED;
                }
                case 24 -> ping = (int) in.readEnum();
                default -> in.skipField(tag);
            }
        }
        return new ApolloPingData(server, status, ping);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (!server.isEmpty()) out.writeString(1, server);
        if (status.number >= 1) {
            out.writeRawByte(16);
            out.writeVarint32(status.number);
        }
        if (ping != 0) out.writeInt32(3, ping);
    }
}
