package com.adjustclient.utilities.backend.api.request.config;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteConfigurationRequest extends BackendRequest {
    public DeleteConfigurationRequest(com.adjustclient.managers.config.ConfigurationManager configuration) {
        super("/configuration/deleteConfiguration", 45000L, new ArrayList<>(Arrays.asList(configuration)));
    }
}
