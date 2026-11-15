package com.adjustclient.lunar.apollo.protocol.tntcountdown;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.*;
import com.adjustclient.util.proto.ProtoReader;

public final class SetTntCountdownMessage implements ProtoMessage {
    public Object field8;
    public ApolloEntityId entityId = null;

    public static SetTntCountdownMessage parse(ProtoReader reader) {
        SetTntCountdownMessage message = new SetTntCountdownMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.entityId = ApolloEntityId.parse(reader.readMessage()); break;
                case 16: reader.readEnum();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
