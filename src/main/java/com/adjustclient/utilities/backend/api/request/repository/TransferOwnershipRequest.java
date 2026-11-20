package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class TransferOwnershipRequest extends BackendRequest {
    public TransferOwnershipRequest(com.adjustclient.core.gui.account.AccountEntry account) {
        super(SET_LAST_LOGIN, 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
