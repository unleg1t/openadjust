package com.adjustclient.utilities.lunar.apollo.protocol.cooldown;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class RemoveCooldownMessage implements ProtoMessage {
    public Object field8;
    public String name = "";

    public static RemoveCooldownMessage parse(ProtoReader reader) {
        RemoveCooldownMessage message = new RemoveCooldownMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) message.name = reader.readString();
            else reader.skipField(tag);
        }
        return message;
    }
}
