package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RemoveFriendRequest extends BackendRequest {
    public RemoveFriendRequest(com.adjustclient.user.ClientUser name) {
        super("/friends/removeFriend", 45000L, new ArrayList<>(Arrays.asList(name)));
    }
}
