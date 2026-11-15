package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UnblockUserRequest extends BackendRequest {
    public UnblockUserRequest(Object... fields) {
        super("/friends/sendConfiguration", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
