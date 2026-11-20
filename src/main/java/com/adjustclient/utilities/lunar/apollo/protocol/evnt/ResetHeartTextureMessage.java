package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

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
