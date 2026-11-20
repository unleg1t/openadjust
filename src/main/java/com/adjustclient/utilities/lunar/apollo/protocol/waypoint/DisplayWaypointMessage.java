package com.adjustclient.utilities.lunar.apollo.protocol.waypoint;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;

public final class DisplayWaypointMessage implements ProtoMessage {
    public Object field16;
    public String name = "";
    public ApolloBlockLocation location = null;
    public ApolloColor color = null;

    public static DisplayWaypointMessage parse(ProtoReader reader) {
        DisplayWaypointMessage message = new DisplayWaypointMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.name = reader.readString(); break;
                case 18: message.location = ApolloBlockLocation.parse(reader.readMessage()); break;
                case 26: message.color = ApolloColor.parse(reader.readMessage()); break;
                case 32: reader.readEnum();
                case 40: reader.readEnum();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
