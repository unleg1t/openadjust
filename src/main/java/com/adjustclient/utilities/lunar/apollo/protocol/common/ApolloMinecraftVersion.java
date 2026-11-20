package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;

public final class ApolloMinecraftVersion implements ProtoMessage {
    private final String name;
    private final Object field8 = null;

    public ApolloMinecraftVersion(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public static ApolloMinecraftVersion parse(ProtoReader in) {
        String name = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: name = in.readString(); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloMinecraftVersion(name);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (!name.isEmpty()) out.writeString(1, name);
    }
}
