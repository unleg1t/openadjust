package com.adjustclient.backend.api.request.polls;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class VoteRequest extends BackendRequest {
    public VoteRequest(int option) {
        super("/polls/vote", 45000L, new ArrayList<>(Arrays.asList(option)));
    }
}
