package com.adjustclient.backend.api.request.friends;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DenyFriendRequest extends BackendRequest {
    public DenyFriendRequest(int invitationId, boolean accepted) {
        super("/friends/inviteAction", 45000L,
                new ArrayList<>(Arrays.asList(invitationId, accepted)));
    }
}
