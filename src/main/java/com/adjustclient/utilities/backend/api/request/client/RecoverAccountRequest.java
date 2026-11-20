package com.adjustclient.utilities.backend.api.request.client;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RecoverAccountRequest extends BackendRequest {
    public RecoverAccountRequest() {
        super("/client/queryTagData", 45000L, new ArrayList<>());
    }
}
