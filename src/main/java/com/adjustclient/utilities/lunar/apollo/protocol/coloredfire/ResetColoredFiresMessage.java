package com.adjustclient.utilities.lunar.apollo.protocol.coloredfire;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetColoredFiresMessage implements ProtoMessage {
    public Object field4;

    public static ResetColoredFiresMessage parse(ProtoReader reader) {
        ResetColoredFiresMessage message = new ResetColoredFiresMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
