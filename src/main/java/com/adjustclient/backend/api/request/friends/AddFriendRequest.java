package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class AddFriendRequest extends BackendRequest {
    public AddFriendRequest(com.adjustclient.user.ClientUser name) {
        super("/friends/addFriend", 45000L, new ArrayList<>(Arrays.asList(name)));
    }
}
