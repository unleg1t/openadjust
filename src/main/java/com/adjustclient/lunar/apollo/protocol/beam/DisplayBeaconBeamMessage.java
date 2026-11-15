package com.adjustclient.lunar.apollo.protocol.beam;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.*;
import com.adjustclient.util.proto.ProtoReader;

public final class DisplayBeaconBeamMessage implements ProtoMessage {
    public Object field16;
    public String id = "";
    public ApolloColor color = null;
    public ApolloBlockLocation location = null;

    public static DisplayBeaconBeamMessage parse(ProtoReader reader) {
        DisplayBeaconBeamMessage message = new DisplayBeaconBeamMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.id = reader.readString(); break;
                case 18: message.color = ApolloColor.parse(reader.readMessage()); break;
                case 26: message.location = ApolloBlockLocation.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
