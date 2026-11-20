package com.adjustclient.utilities.lunar.apollo.protocol.pingmarker;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;

public final class SetPingMarkerTypeMessage implements ProtoMessage {
    public Object field12;
    public String id = "";
    public ApolloIcon icon = null;

    public static SetPingMarkerTypeMessage parse(ProtoReader reader) {
        SetPingMarkerTypeMessage message = new SetPingMarkerTypeMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.id = reader.readString(); break;
                case 18: message.icon = ApolloIcon.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
