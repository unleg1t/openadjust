package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GenerateInviteCodeRequest extends BackendRequest {
    public GenerateInviteCodeRequest() {
        super("/client/generateInvite", 45000L, new ArrayList<>(Arrays.asList()));
    }
}
