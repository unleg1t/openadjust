package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetAccountDataRequest extends BackendRequest {
    public GetAccountDataRequest(com.adjustclient.ui.account.AccountEntry account) {
        super("/repository/getAccountData", 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
