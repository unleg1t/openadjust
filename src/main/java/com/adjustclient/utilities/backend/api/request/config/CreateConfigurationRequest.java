package com.adjustclient.utilities.backend.api.request.config;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateConfigurationRequest extends BackendRequest {
    public CreateConfigurationRequest(String name, byte[] data) {
        super("/configuration/createConfiguration", 45000L, new ArrayList<>(Arrays.asList(name, data)));
    }
}
