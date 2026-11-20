package com.adjustclient.utilities.backend.api.request.config;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public final class UpdateConfigurationRequest extends BackendRequest {
    public UpdateConfigurationRequest(Object... fields) {
        super(UPDATE_GLOBAL_CONFIGURATION, 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
