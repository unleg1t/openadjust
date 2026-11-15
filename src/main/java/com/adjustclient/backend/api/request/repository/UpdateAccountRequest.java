package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UpdateAccountRequest extends BackendRequest {
    public UpdateAccountRequest(com.adjustclient.ui.account.AccountEntry account, String name, java.util.UUID uuid, String token) {
        super("/repository/updateAccountSession", 45000L,
                new ArrayList<>(Arrays.asList(account, java.util.Objects.requireNonNull(name),
                        new com.adjustclient.backend.api.NullableValue(uuid), java.util.Objects.requireNonNull(token))));
    }
}
