package com.adjustclient.utilities.lunar.apollo.protocol.border;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class RemoveBorderMessage implements ProtoMessage {
    public String id;
    public Object field8;

    public static RemoveBorderMessage parse(ProtoReader reader) {
        String id = "";
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> id = reader.readString();
                default -> reader.skipField(tag);
            }
        }
        RemoveBorderMessage message = new RemoveBorderMessage();
        message.id = id;
        return message;
    }
}
