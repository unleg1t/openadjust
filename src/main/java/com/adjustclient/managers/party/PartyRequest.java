package com.adjustclient.managers.party;

import java.util.function.Consumer;

public class PartyRequest {
    public Consumer<com.adjustclient.utilities.server.ServerInfo> callback;
    public int requestId;

    public PartyRequest(int requestId, Consumer<com.adjustclient.utilities.server.ServerInfo> callback) {
        this.requestId = requestId;
        this.callback = callback;
    }
}
