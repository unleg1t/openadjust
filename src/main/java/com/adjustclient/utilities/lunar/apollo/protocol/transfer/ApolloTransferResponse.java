package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;

public final class ApolloTransferResponse implements ProtoMessage {
    private final byte[] requestId;
    private final ApolloTransferStatus status;
    private final Object field12 = null;

    public ApolloTransferResponse(byte[] requestId, ApolloTransferStatus status) {
        this.requestId = requestId;
        this.status = status;
    }

    public byte[] getRequestId() { return requestId; }
    public ApolloTransferStatus getStatus() { return status; }

    public static ApolloTransferResponse parse(ProtoReader in) {
        byte[] requestId = new byte[0];
        ApolloTransferStatus status = ApolloTransferStatus.STATUS_UNSPECIFIED;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> requestId = in.readBytes();
                case 16 -> {
                    long value = in.readEnum();
                    status = value == 0 ? ApolloTransferStatus.STATUS_UNSPECIFIED
                            : value == 1 ? ApolloTransferStatus.STATUS_ACCEPTED
                            : value == 2 ? ApolloTransferStatus.STATUS_REJECTED
                            : ApolloTransferStatus.UNRECOGNIZED;
                }
                default -> in.skipField(tag);
            }
        }
        return new ApolloTransferResponse(requestId, status);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (requestId.length != 0) out.writeBytes(1, requestId, 0, requestId.length);
        if (status.number >= 1) {
            out.writeRawByte(16);
            out.writeVarint32(status.number);
        }
    }
}
