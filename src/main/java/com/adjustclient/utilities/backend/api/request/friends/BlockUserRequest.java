package com.adjustclient.utilities.backend.api.request.friends;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class BlockUserRequest extends BackendRequest {
    public BlockUserRequest(com.adjustclient.managers.user.ClientUser user, boolean blocked) {
        super("/friends/blockUser", 45000L, new ArrayList<>(Arrays.asList(user, blocked)));
    }
}
