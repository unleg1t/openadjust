package com.adjustclient.backend.api.request.polls;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetPollRequest extends BackendRequest {
    public GetPollRequest(Object... fields) {
        super("/polls/subscribeStatus", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
