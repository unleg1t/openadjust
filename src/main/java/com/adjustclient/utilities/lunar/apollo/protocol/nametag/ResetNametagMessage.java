package com.adjustclient.utilities.lunar.apollo.protocol.nametag;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetNametagMessage implements ProtoMessage {
    public Object field8;
    public ApolloUuid playerUuid = null;

    public static ResetNametagMessage parse(ProtoReader reader) {
        ResetNametagMessage message = new ResetNametagMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
