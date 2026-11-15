package com.adjustclient.lunar.apollo.protocol.border;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetBordersMessage implements ProtoMessage {
    public Object field4;

    public static ResetBordersMessage parse(ProtoReader reader) {
        for (int tag; (tag = reader.readTag()) != 0;) {
            reader.skipField(tag);
        }
        ResetBordersMessage message = new ResetBordersMessage();
        return message;
    }
}
