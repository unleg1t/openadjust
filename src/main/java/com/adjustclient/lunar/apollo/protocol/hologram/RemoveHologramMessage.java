package com.adjustclient.lunar.apollo.protocol.hologram;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class RemoveHologramMessage implements ProtoMessage {
    public Object field8;
    public String id = "";

    public static RemoveHologramMessage parse(ProtoReader reader) {
        RemoveHologramMessage message = new RemoveHologramMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) message.id = reader.readString();
            else reader.skipField(tag);
        }
        return message;
    }
}
