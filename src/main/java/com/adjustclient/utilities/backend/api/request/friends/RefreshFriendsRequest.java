package com.adjustclient.utilities.backend.api.request.friends;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RefreshFriendsRequest extends BackendRequest {
    public RefreshFriendsRequest(Object... fields) {
        super("/friends/queryBlockList", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
