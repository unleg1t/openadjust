package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteAccountRequest extends BackendRequest {
    public DeleteAccountRequest(com.adjustclient.core.gui.account.AccountEntry account) {
        super("/repository/deleteAccount", 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
