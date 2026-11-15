package com.adjustclient.lunar.apollo.protocol.player;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;
import com.adjustclient.util.proto.ProtoWriter;

public final class ModMessage implements ProtoMessage {
    private final String id;
    private final String name;
    private final String version;
    private final ApolloModType type;
    private final Object field20 = null;

    public ModMessage(String id, String name, String version, ApolloModType type) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public ApolloModType getType() { return type; }

    public static ModMessage parse(ProtoReader in) {
        String id = "";
        String name = "";
        String version = "";
        ApolloModType type = ApolloModType.TYPE_UNSPECIFIED;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> id = in.readString();
                case 18 -> name = in.readString();
                case 26 -> version = in.readString();
                case 32 -> {
                    long value = in.readEnum();
                    type = value == 0 ? ApolloModType.TYPE_UNSPECIFIED
                            : value == 1 ? ApolloModType.TYPE_FABRIC_INTERNAL
                            : value == 2 ? ApolloModType.TYPE_FABRIC_EXTERNAL
                            : value == 3 ? ApolloModType.TYPE_FORGE_INTERNAL
                            : value == 4 ? ApolloModType.TYPE_FORGE_EXTERNAL
                            : ApolloModType.UNRECOGNIZED;
                }
                default -> in.skipField(tag);
            }
        }
        return new ModMessage(id, name, version, type);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (!id.isEmpty()) out.writeString(1, id);
        if (!name.isEmpty()) out.writeString(2, name);
        if (!version.isEmpty()) out.writeString(3, version);
        if (type.number >= 1) {
            out.writeRawByte(32);
            out.writeVarint32(type.number);
        }
    }
}
