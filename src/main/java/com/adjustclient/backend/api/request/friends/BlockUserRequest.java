package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class BlockUserRequest extends BackendRequest {
    public BlockUserRequest(com.adjustclient.user.ClientUser user, boolean blocked) {
        super("/friends/blockUser", 45000L, new ArrayList<>(Arrays.asList(user, blocked)));
    }
}
