package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessageRegistry;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.util.Objects;

public final class ProtoAny implements ProtoMessage {
    private final String typeUrl;
    private final byte[] data;
    private final Object field12 = null;

    public ProtoAny(String typeUrl, byte[] data) {
        this.typeUrl = typeUrl;
        this.data = data.clone();
    }

    public String getTypeUrl() { return typeUrl; }
    public byte[] getData() { return data; }

    public static ProtoAny parse(ProtoReader in) {
        String typeUrl = "";
        byte[] data = new byte[0];
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: typeUrl = in.readString(); break;
                case 18: data = in.readBytes(); break;
                default: in.skipField(tag);
            }
        }
        return new ProtoAny(typeUrl, data);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (!typeUrl.isEmpty()) out.writeString(1, typeUrl);
        if (data.length != 0) out.writeBytes(2, data);
    }

    public boolean is(Class<? extends ProtoMessage> messageClass) {
        // c preserves the registry's "Unregistered protobuf message class: " error.
        String expected = ProtoMessageRegistry.c(messageClass).typeName();
        return Objects.requireNonNull(expected, "protobufTypeName").equals(getTypeName());
    }

    public static ProtoAny pack(ProtoMessage message) {
        String typeName = ProtoMessageRegistry.c(message.getClass()).typeName();
        String typeUrl = "type.googleapis.com" + "/" + typeName;
        return new ProtoAny(typeUrl, message.toByteArray());
    }

    public <T extends ProtoMessage> T unpack(Class<T> messageClass) {
        String expected = ProtoMessageRegistry.c(messageClass).typeName();
        if (!Objects.requireNonNull(expected, "protobufTypeName").equals(getTypeName())) {
            throw new IllegalArgumentException("Expected " + expected + " but Any contains " + getTypeName());
        }
        byte[] bytes = Objects.requireNonNull(data, "data");
        return ProtoMessageRegistry.c(messageClass).parser().parse(bytes);
    }

    public String getTypeName() {
        int slash = typeUrl.lastIndexOf('/');
        return slash < 0 ? typeUrl : typeUrl.substring(slash + 1);
    }
}
