package com.adjustclient.backend.api.request.config;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteConfigurationRequest extends BackendRequest {
    public DeleteConfigurationRequest(com.adjustclient.service.config.ConfigurationManager configuration) {
        super("/configuration/deleteConfiguration", 45000L, new ArrayList<>(Arrays.asList(configuration)));
    }
}
