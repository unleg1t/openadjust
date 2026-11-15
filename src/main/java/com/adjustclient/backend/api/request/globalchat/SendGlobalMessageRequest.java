package com.adjustclient.backend.api.request.globalchat;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SendGlobalMessageRequest extends BackendRequest {
    public SendGlobalMessageRequest(String message) {
        super("/globalchat/sendMessage", 45000L, new ArrayList<>(Arrays.asList(message)));
    }
}
