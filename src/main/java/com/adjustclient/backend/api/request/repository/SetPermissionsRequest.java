package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SetPermissionsRequest extends BackendRequest {
    public SetPermissionsRequest(Object... fields) {
        super("/repository/setIpBan", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
