package com.adjustclient.lunar.apollo.protocol.beam;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class RemoveBeaconBeamMessage implements ProtoMessage {
    public Object field8;
    public String id = "";

    public static RemoveBeaconBeamMessage parse(ProtoReader reader) {
        RemoveBeaconBeamMessage message = new RemoveBeaconBeamMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) message.id = reader.readString();
            else reader.skipField(tag);
        }
        return message;
    }
}
