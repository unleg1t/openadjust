package com.adjustclient.utilities.lunar.apollo.protocol.hologram;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetHologramsMessage implements ProtoMessage {
    public Object field4;

    public static ResetHologramsMessage parse(ProtoReader reader) {
        ResetHologramsMessage message = new ResetHologramsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
