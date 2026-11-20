package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class AddAccountRequest extends BackendRequest {
    public AddAccountRequest(Object... fields) {
        super(CHECK_IP_BAN, 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
