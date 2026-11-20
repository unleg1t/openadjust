package com.adjustclient.utilities.backend.api.request.config;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SaveUserConfigurationRequest extends BackendRequest {
    public SaveUserConfigurationRequest(byte[] data) {
        super("/configuration/saveUserConfiguration", 45000L, new ArrayList<>(Arrays.asList(data)));
    }
}
