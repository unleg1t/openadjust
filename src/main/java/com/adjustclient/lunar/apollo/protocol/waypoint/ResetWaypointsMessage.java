package com.adjustclient.lunar.apollo.protocol.waypoint;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetWaypointsMessage implements ProtoMessage {
    public Object field4;

    public static ResetWaypointsMessage parse(ProtoReader reader) {
        ResetWaypointsMessage message = new ResetWaypointsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
