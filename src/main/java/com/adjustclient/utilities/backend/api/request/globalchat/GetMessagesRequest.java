package com.adjustclient.utilities.backend.api.request.globalchat;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetMessagesRequest extends BackendRequest {
    public GetMessagesRequest(Object... fields) {
        super(GET_MESSAGE_HISTORY, 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
