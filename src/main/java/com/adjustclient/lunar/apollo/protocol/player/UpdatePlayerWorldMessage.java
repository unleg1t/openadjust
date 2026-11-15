package com.adjustclient.lunar.apollo.protocol.player;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class UpdatePlayerWorldMessage implements ProtoMessage {
    private final String world;
    private final Object field8 = null;

    public UpdatePlayerWorldMessage(String world) {
        this.world = world;
    }

    public String getWorld() { return world; }

    public static UpdatePlayerWorldMessage parse(ProtoReader in) {
        String world = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> world = in.readString();
                default -> in.skipField(tag);
            }
        }
        return new UpdatePlayerWorldMessage(world);
    }
}
