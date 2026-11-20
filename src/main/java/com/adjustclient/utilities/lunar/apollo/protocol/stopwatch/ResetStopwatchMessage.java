package com.adjustclient.utilities.lunar.apollo.protocol.stopwatch;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetStopwatchMessage implements ProtoMessage {
    public Object field4;

    public static ResetStopwatchMessage parse(ProtoReader reader) {
        ResetStopwatchMessage message = new ResetStopwatchMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
