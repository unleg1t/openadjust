package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class InviteUserRequest extends BackendRequest {
    public InviteUserRequest(String user, Object repository) {
        super(KICK_REPOSITORY_USER, 45000L, new ArrayList<>(Arrays.asList(user, repository)));
    }
}
