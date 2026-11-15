package com.adjustclient.lunar.apollo.protocol.waypoint;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class RemoveWaypointMessage implements ProtoMessage {
    public Object field8;
    public String name = "";

    public static RemoveWaypointMessage parse(ProtoReader reader) {
        RemoveWaypointMessage message = new RemoveWaypointMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) message.name = reader.readString();
            else reader.skipField(tag);
        }
        return message;
    }
}
