package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SendCustomPayloadRequest extends BackendRequest {
    public SendCustomPayloadRequest(com.adjustclient.user.ClientUser user, int packetId, byte[] payload) {
        super("/client/sendCustomPayload", 45000L, new ArrayList<>(Arrays.asList(user, packetId, payload)));
    }
}
