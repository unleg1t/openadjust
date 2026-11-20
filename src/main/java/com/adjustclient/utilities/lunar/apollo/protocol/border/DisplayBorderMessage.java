package com.adjustclient.utilities.lunar.apollo.protocol.border;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloColor;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloCuboid2D;
import com.adjustclient.utilities.proto.ProtoReader;

public final class DisplayBorderMessage implements ProtoMessage {
    public String id;
    public String world;
    public ApolloColor color;
    public ApolloCuboid2D bounds;
    public Object field20;

    public static DisplayBorderMessage parse(ProtoReader reader) {
        String id = "";
        String world = "";
        ApolloColor color = null;
        ApolloCuboid2D bounds = null;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> id = reader.readString();
                case 18 -> world = reader.readString();
                case 24, 32, 40, 64 -> reader.readEnum();
                case 50 -> color = ApolloColor.parse(reader.readMessage());
                case 58 -> bounds = ApolloCuboid2D.parse(reader.readMessage());
                default -> reader.skipField(tag);
            }
        }
        DisplayBorderMessage message = new DisplayBorderMessage();
        message.id = id;
        message.world = world;
        message.color = color;
        message.bounds = bounds;
        return message;
    }
}
