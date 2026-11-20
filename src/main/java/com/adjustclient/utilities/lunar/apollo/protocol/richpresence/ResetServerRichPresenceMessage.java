package com.adjustclient.utilities.lunar.apollo.protocol.richpresence;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetServerRichPresenceMessage implements ProtoMessage {
    public Object field4;

    public static ResetServerRichPresenceMessage parse(ProtoReader reader) {
        ResetServerRichPresenceMessage message = new ResetServerRichPresenceMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
