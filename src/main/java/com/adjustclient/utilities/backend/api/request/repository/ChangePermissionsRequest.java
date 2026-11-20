package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class ChangePermissionsRequest extends BackendRequest {
    public ChangePermissionsRequest(Object... fields) {
        super("/repository/createAltAccount", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
