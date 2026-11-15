package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class FailCheckRequest extends BackendRequest {
    public FailCheckRequest(byte checkId, String details) {
        super("/client/failCheck", 45000L, new ArrayList<>(Arrays.asList(checkId, details)));
    }
}
