package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class GetAccountDataRequest extends BackendRequest {
    public GetAccountDataRequest(com.adjustclient.core.gui.account.AccountEntry account) {
        super("/repository/getAccountData", 45000L, new ArrayList<>(Arrays.asList(account)));
    }
}
