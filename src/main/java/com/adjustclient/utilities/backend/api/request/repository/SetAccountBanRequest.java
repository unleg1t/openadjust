package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SetAccountBanRequest extends BackendRequest {
    public SetAccountBanRequest(com.adjustclient.core.gui.account.AccountEntry account, long expiry) {
        super("/repository/setAccountBan", 45000L, new ArrayList<>(Arrays.asList(account, expiry)));
    }
}
