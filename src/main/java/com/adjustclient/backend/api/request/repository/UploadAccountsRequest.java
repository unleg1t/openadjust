package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UploadAccountsRequest extends BackendRequest {
    public UploadAccountsRequest(Object repository, String name) {
        super("/repository/updateRepositoryName", 45000L, new ArrayList<>(Arrays.asList(repository, name)));
    }
}
