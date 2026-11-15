package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetFriendsRequest extends BackendRequest {
    public GetFriendsRequest(String name, Object repository) {
        super(INVITE_FRIENDS_REPOSITORY, 45000L, new ArrayList<>(Arrays.asList(name, repository)));
    }
}
