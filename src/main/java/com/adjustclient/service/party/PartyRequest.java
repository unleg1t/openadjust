package com.adjustclient.service.party;

import java.util.function.Consumer;

public class PartyRequest {
    public Consumer<com.adjustclient.server.ServerInfo> callback;
    public int requestId;

    public PartyRequest(int requestId, Consumer<com.adjustclient.server.ServerInfo> callback) {
        this.requestId = requestId;
        this.callback = callback;
    }
}
