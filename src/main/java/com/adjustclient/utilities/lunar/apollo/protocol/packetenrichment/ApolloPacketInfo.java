package com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoTimestamp;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloPacketInfo implements ProtoMessage {
    private final ProtoTimestamp timestamp;
    private final Object field8 = null;

    public ApolloPacketInfo(ProtoTimestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ProtoTimestamp getTimestamp() {
        return timestamp;
    }

    public static ApolloPacketInfo parse(ProtoReader in) {
        ProtoTimestamp timestamp = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> timestamp = ProtoTimestamp.parse(in.readMessage());
                default -> in.skipField(tag);
            }
        }
        return new ApolloPacketInfo(timestamp);
    }
}
