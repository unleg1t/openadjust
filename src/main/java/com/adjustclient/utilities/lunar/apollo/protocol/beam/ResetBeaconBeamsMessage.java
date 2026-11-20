package com.adjustclient.utilities.lunar.apollo.protocol.beam;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetBeaconBeamsMessage implements ProtoMessage {
    public Object field4;

    public static ResetBeaconBeamsMessage parse(ProtoReader reader) {
        ResetBeaconBeamsMessage message = new ResetBeaconBeamsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
