package com.adjustclient.utilities.backend.api.request.config;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetFeaturedConfigurationsRequest extends BackendRequest {
    public GetFeaturedConfigurationsRequest(Object... fields) {
        super("/configuration/renameConfiguration", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
