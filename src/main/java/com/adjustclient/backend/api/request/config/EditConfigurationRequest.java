package com.adjustclient.backend.api.request.config;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public final class EditConfigurationRequest extends BackendRequest {
    public EditConfigurationRequest(Object... fields) {
        super("/configuration/editDescriptionGlobalConfiguration", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
