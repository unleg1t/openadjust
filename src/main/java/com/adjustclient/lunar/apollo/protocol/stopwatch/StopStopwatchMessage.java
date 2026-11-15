package com.adjustclient.lunar.apollo.protocol.stopwatch;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class StopStopwatchMessage implements ProtoMessage {
    public Object field4;

    public static StopStopwatchMessage parse(ProtoReader reader) {
        StopStopwatchMessage message = new StopStopwatchMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
