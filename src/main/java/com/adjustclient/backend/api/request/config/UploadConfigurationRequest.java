package com.adjustclient.backend.api.request.config;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UploadConfigurationRequest extends BackendRequest {
    public UploadConfigurationRequest(Object... fields) {
        super("/configuration/voteGlobalConfiguration", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
