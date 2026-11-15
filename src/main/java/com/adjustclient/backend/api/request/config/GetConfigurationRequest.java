package com.adjustclient.backend.api.request.config;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public final class GetConfigurationRequest extends BackendRequest {
    public GetConfigurationRequest(Object... fields) {
        super(REMOVE_GLOBAL_CONFIGURATION, 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
