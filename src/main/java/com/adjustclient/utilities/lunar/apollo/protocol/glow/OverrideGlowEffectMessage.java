package com.adjustclient.utilities.lunar.apollo.protocol.glow;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class OverrideGlowEffectMessage implements ProtoMessage {
    public Object field12;
    public ApolloUuid playerUuid = null;
    public ApolloColor color = null;

    public static OverrideGlowEffectMessage parse(ProtoReader reader) {
        OverrideGlowEffectMessage message = new OverrideGlowEffectMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                case 18: message.color = ApolloColor.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
