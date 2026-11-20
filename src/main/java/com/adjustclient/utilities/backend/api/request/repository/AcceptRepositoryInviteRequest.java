package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class AcceptRepositoryInviteRequest extends BackendRequest {
    public AcceptRepositoryInviteRequest(Object repository) {
        super("/repository/accessTime", 45000L, new ArrayList<>(Arrays.asList(repository)));
    }
}
