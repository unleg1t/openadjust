package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class MessageRequest extends BackendRequest {
    public MessageRequest(com.adjustclient.user.ClientUser user, String message) {
        super("/friends/message", 45000L, new ArrayList<>(Arrays.asList(user, message)));
    }
}
