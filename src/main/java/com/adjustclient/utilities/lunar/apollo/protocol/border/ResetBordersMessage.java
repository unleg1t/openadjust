package com.adjustclient.utilities.lunar.apollo.protocol.border;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

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
