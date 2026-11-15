package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class TransferOwnershipRequest extends BackendRequest {
    public TransferOwnershipRequest(com.adjustclient.ui.account.AccountEntry account) {
        super(SET_LAST_LOGIN, 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
