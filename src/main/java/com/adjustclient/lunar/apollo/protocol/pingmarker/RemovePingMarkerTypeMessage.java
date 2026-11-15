package com.adjustclient.lunar.apollo.protocol.pingmarker;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class RemovePingMarkerTypeMessage implements ProtoMessage {
    public Object field8;
    public String id = "";

    public static RemovePingMarkerTypeMessage parse(ProtoReader reader) {
        RemovePingMarkerTypeMessage message = new RemovePingMarkerTypeMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) message.id = reader.readString();
            else reader.skipField(tag);
        }
        return message;
    }
}
