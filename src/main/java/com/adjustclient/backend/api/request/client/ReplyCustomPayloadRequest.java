package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class ReplyCustomPayloadRequest extends BackendRequest {
    public ReplyCustomPayloadRequest(int payloadId, boolean accepted) {
        super("/client/replyCustomPayload", 45000L, new ArrayList<>(Arrays.asList(payloadId, accepted)));
    }
}
