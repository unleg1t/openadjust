package com.adjustclient.utilities.lunar.apollo.protocol.pingmarker;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class DisplayPlayerPingMessage implements ProtoMessage {
    public Object field32;
    public ApolloUuid playerUuid = null;
    public String markerType = "";
    public ApolloLocation location = null;
    public ApolloColor color = null;
    public ApolloIcon icon = null;
    public ApolloSound sound = null;
    public ProtoDuration duration = null;

    public static DisplayPlayerPingMessage parse(ProtoReader reader) {
        DisplayPlayerPingMessage message = new DisplayPlayerPingMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                case 18: message.markerType = reader.readString(); break;
                case 26: message.location = ApolloLocation.parse(reader.readMessage()); break;
                case 34: message.color = ApolloColor.parse(reader.readMessage()); break;
                case 42: message.icon = ApolloIcon.parse(reader.readMessage()); break;
                case 50: message.sound = ApolloSound.parse(reader.readMessage()); break;
                case 58: message.duration = ProtoDuration.parse(reader.readMessage()); break;
                case 64: reader.readEnum();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
