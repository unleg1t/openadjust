package com.adjustclient.utilities.backend.api.request.friends;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RemoveFriendRequest extends BackendRequest {
    public RemoveFriendRequest(com.adjustclient.managers.user.ClientUser name) {
        super("/friends/removeFriend", 45000L, new ArrayList<>(Arrays.asList(name)));
    }
}
