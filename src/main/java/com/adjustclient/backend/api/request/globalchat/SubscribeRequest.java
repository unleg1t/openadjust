package com.adjustclient.backend.api.request.globalchat;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public final class SubscribeRequest extends BackendRequest {
    public SubscribeRequest(Object... fields) {
        super("/globalchat/unsubscribe", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
