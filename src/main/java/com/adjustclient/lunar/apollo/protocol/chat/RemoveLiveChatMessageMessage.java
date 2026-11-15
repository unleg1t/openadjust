package com.adjustclient.lunar.apollo.protocol.chat;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class RemoveLiveChatMessageMessage implements ProtoMessage {
    public Object field4;

    public static RemoveLiveChatMessageMessage parse(ProtoReader reader) {
        RemoveLiveChatMessageMessage message = new RemoveLiveChatMessageMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 8) reader.readEnum();
            else reader.skipField(tag);
        }
        return message;
    }
}
