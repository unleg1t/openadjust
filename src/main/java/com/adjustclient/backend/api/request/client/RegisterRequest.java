package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RegisterRequest extends BackendRequest {
    public RegisterRequest(long recoveryKeyHash) {
        super("/client/setRecoveryKey", 45000L, new ArrayList<>(Arrays.asList(recoveryKeyHash)));
    }
}
