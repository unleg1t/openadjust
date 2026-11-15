package com.adjustclient.lunar.apollo.protocol.title;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetTitlesMessage implements ProtoMessage {
    public Object field4;

    public static ResetTitlesMessage parse(ProtoReader reader) {
        ResetTitlesMessage message = new ResetTitlesMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
