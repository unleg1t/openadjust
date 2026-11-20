package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.util.ArrayList;
import java.util.List;

public final class ApolloPingResponse implements ProtoMessage {
    private final byte[] requestId;
    private final List<ApolloPingData> pingData;
    private final Object field12 = null;

    public ApolloPingResponse(byte[] requestId, List<ApolloPingData> pingData) {
        this.requestId = requestId;
        this.pingData = pingData;
    }

    public byte[] getRequestId() { return requestId; }
    public List<ApolloPingData> getPingData() { return pingData; }

    public static ApolloPingResponse parse(ProtoReader in) {
        byte[] requestId = new byte[0];
        List<ApolloPingData> pingData = new ArrayList<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> requestId = in.readBytes();
                case 18 -> pingData.add(ApolloPingData.parse(in.readMessage()));
                default -> in.skipField(tag);
            }
        }
        return new ApolloPingResponse(requestId, pingData);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (requestId.length != 0) out.writeBytes(1, requestId);
        for (ApolloPingData data : pingData) {
            byte[] bytes = data.toByteArray();
            out.writeBytes(2, bytes);
        }
    }
}
