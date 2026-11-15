package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteAccountRequest extends BackendRequest {
    public DeleteAccountRequest(com.adjustclient.ui.account.AccountEntry account) {
        super("/repository/deleteAccount", 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
