package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloTransferRequest implements ProtoMessage {
    private final byte[] requestId;
    private final String server;
    private final Object field12 = null;

    public ApolloTransferRequest(byte[] requestId, String server) {
        this.requestId = requestId;
        this.server = server;
    }

    public byte[] getRequestId() { return requestId; }

    public String getServer() { return server; }

    public static ApolloTransferRequest parse(ProtoReader in) {
        byte[] requestId = new byte[0];
        String server = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> requestId = in.readBytes();
                case 18 -> server = in.readString();
                default -> in.skipField(tag);
            }
        }
        return new ApolloTransferRequest(requestId, server);
    }
}
