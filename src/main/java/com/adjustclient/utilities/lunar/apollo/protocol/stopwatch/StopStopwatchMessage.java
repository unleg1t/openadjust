package com.adjustclient.utilities.lunar.apollo.protocol.stopwatch;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

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
