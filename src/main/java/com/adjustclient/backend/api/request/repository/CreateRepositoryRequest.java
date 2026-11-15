package com.adjustclient.backend.api.request.repository;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateRepositoryRequest extends BackendRequest {
    public CreateRepositoryRequest(String name) {
        super("/repository/create", 45000L, new ArrayList<>(Arrays.asList(name)));
    }
}
