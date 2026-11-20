package com.adjustclient.utilities.backend.api.request.repository;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteRepositoryRequest extends BackendRequest {
    public DeleteRepositoryRequest(Object repository, boolean deleteAccounts) {
        super("/repository/delete", 45000L, new ArrayList<>(Arrays.asList(repository, deleteAccounts)));
    }
}
