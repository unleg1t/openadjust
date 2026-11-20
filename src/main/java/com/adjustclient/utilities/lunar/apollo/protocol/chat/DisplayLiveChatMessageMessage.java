package com.adjustclient.utilities.lunar.apollo.protocol.chat;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class DisplayLiveChatMessageMessage implements ProtoMessage {
    public Object field8;
    public String text = "";

    public static DisplayLiveChatMessageMessage parse(ProtoReader reader) {
        DisplayLiveChatMessageMessage message = new DisplayLiveChatMessageMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 8: reader.readEnum();
                case 18: message.text = reader.readString(); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
