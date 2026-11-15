package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UpdateClientRequest extends BackendRequest {
    public UpdateClientRequest() {
        super("/client/wantsHudColors", 45000L, new ArrayList<>());
    }
}
