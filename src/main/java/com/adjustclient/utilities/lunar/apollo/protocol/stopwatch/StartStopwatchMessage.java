package com.adjustclient.utilities.lunar.apollo.protocol.stopwatch;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class StartStopwatchMessage implements ProtoMessage {
    public Object field4;

    public static StartStopwatchMessage parse(ProtoReader reader) {
        StartStopwatchMessage message = new StartStopwatchMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
