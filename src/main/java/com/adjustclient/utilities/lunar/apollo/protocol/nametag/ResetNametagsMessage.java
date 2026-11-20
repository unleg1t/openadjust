package com.adjustclient.utilities.lunar.apollo.protocol.nametag;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

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
