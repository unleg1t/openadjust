package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteInviteRequest extends BackendRequest {
    public DeleteInviteRequest(Object repository, com.adjustclient.ui.account.AccountEntry[] accounts) {
        super("/repository/deleteMultipleAccounts", 45000L, new ArrayList<>(Arrays.asList(repository, accounts)));
    }
}
