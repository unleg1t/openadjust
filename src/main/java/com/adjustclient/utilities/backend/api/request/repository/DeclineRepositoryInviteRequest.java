package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeclineRepositoryInviteRequest extends BackendRequest {
    public DeclineRepositoryInviteRequest(Object repository) {
        super(REQUEST_REPOSITORY_DATA, 45000L, new ArrayList<>(Arrays.asList(repository)));
    }
}
