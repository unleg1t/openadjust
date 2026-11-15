package com.adjustclient.lunar.apollo.protocol.transfer;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class ApolloPingRequest implements ProtoMessage {
    private final byte[] requestId;
    private final List<String> servers;
    private final Object field12 = null;

    public ApolloPingRequest(byte[] requestId, List<String> servers) {
        this.requestId = requestId;
        this.servers = servers;
    }

    public byte[] getRequestId() { return requestId; }

    public List<String> getServers() { return servers; }

    public static ApolloPingRequest parse(ProtoReader in) {
        byte[] requestId = new byte[0];
        List<String> servers = new ArrayList<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> requestId = in.readBytes();
                case 18 -> servers.add(in.readString());
                default -> in.skipField(tag);
            }
        }
        return new ApolloPingRequest(requestId, servers);
    }
}
