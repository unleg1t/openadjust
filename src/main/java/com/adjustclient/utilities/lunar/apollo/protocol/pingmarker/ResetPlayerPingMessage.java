package com.adjustclient.utilities.lunar.apollo.protocol.pingmarker;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetPlayerPingMessage implements ProtoMessage {
    public Object field4;

    public static ResetPlayerPingMessage parse(ProtoReader reader) {
        ResetPlayerPingMessage message = new ResetPlayerPingMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
