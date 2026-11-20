package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class KickUserRequest extends BackendRequest {
    public KickUserRequest(Object source, Object target, com.adjustclient.core.gui.account.AccountEntry[] accounts) {
        super("/repository/moveAccounts", 45000L, new ArrayList<>(Arrays.asList(source, target, accounts)));
    }
}
