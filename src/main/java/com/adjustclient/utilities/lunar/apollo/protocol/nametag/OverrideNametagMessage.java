package com.adjustclient.utilities.lunar.apollo.protocol.nametag;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class OverrideNametagMessage implements ProtoMessage {
    public Object field16;
    public ApolloUuid playerUuid = null;
    public List<ApolloComponent> lines = new ArrayList<>();
    public List<String> field12 = new ArrayList<>();

    public static OverrideNametagMessage parse(ProtoReader reader) {
        OverrideNametagMessage message = new OverrideNametagMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                case 18: message.lines.add(ApolloComponent.parse(reader.readMessage())); break;
                case 26: message.field12.add(reader.readString()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
