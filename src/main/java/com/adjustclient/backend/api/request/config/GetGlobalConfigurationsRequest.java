package com.adjustclient.backend.api.request.config;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public final class GetGlobalConfigurationsRequest extends BackendRequest {
    public GetGlobalConfigurationsRequest() {
        super("/configuration/requestGlobalConfigurations", 45000L, new ArrayList<>(Arrays.asList()));
    }
}
