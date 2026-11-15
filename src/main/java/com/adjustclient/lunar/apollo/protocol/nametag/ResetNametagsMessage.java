package com.adjustclient.lunar.apollo.protocol.nametag;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetNametagsMessage implements ProtoMessage {
    public Object field4;

    public static ResetNametagsMessage parse(ProtoReader reader) {
        ResetNametagsMessage message = new ResetNametagsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
