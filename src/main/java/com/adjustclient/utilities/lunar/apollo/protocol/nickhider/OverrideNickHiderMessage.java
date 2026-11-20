package com.adjustclient.utilities.lunar.apollo.protocol.nickhider;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class OverrideNickHiderMessage implements ProtoMessage {
    public String nickname;
    public Object field8;

    public static OverrideNickHiderMessage parse(ProtoReader reader) {
        String nickname = "";
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> nickname = reader.readString();
                default -> reader.skipField(tag);
            }
        }
        OverrideNickHiderMessage message = new OverrideNickHiderMessage();
        message.nickname = nickname;
        return message;
    }
}
