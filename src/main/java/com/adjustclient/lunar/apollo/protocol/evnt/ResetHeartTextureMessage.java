package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetHeartTextureMessage implements ProtoMessage {
    public Object field4;

    public static ResetHeartTextureMessage parse(ProtoReader reader) {
        for (int tag; (tag = reader.readTag()) != 0;) {
            reader.skipField(tag);
        }
        ResetHeartTextureMessage message = new ResetHeartTextureMessage();
        return message;
    }
}
