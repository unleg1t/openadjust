package com.adjustclient.lunar.apollo.protocol.pingmarker;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetPingMarkerTypesMessage implements ProtoMessage {
    public Object field4;

    public static ResetPingMarkerTypesMessage parse(ProtoReader reader) {
        ResetPingMarkerTypesMessage message = new ResetPingMarkerTypesMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
