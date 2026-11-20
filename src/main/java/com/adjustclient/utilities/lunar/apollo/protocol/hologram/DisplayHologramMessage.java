package com.adjustclient.utilities.lunar.apollo.protocol.hologram;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class DisplayHologramMessage implements ProtoMessage {
    public Object field20;
    public String id = "";
    public ApolloLocation location = null;
    public List<ApolloComponent> lines = new ArrayList<>();
    public List<String> field16 = new ArrayList<>();

    public static DisplayHologramMessage parse(ProtoReader reader) {
        DisplayHologramMessage message = new DisplayHologramMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.id = reader.readString(); break;
                case 18: message.location = ApolloLocation.parse(reader.readMessage()); break;
                case 26: message.lines.add(ApolloComponent.parse(reader.readMessage())); break;
                case 32: reader.readEnum();
                case 40: reader.readEnum();
                case 48: reader.readEnum();
                case 58: message.field16.add(reader.readString()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
