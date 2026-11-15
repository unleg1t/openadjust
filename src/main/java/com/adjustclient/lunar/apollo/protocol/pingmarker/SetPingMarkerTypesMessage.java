package com.adjustclient.lunar.apollo.protocol.pingmarker;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class SetPingMarkerTypesMessage implements ProtoMessage {
    public List<SetPingMarkerTypeMessage> types = new ArrayList<>();
    public Object field8;

    public static SetPingMarkerTypesMessage parse(ProtoReader reader) {
        SetPingMarkerTypesMessage message = new SetPingMarkerTypesMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) {
                message.types.add(SetPingMarkerTypeMessage.parse(reader.readMessage()));
            } else {
                reader.skipField(tag);
            }
        }
        return message;
    }
}
