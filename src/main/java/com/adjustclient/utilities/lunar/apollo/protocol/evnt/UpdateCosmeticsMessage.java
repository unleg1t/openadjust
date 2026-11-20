package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class UpdateCosmeticsMessage implements ProtoMessage {
    public List<String> cosmetics1;
    public List<String> cosmetics2;
    public Object field12;

    public static UpdateCosmeticsMessage parse(ProtoReader reader) {
        List<String> cosmetics1 = new ArrayList<>();
        List<String> cosmetics2 = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> cosmetics1.add(reader.readString());
                case 18 -> cosmetics2.add(reader.readString());
                default -> reader.skipField(tag);
            }
        }
        UpdateCosmeticsMessage message = new UpdateCosmeticsMessage();
        message.cosmetics1 = cosmetics1;
        message.cosmetics2 = cosmetics2;
        return message;
    }
}
