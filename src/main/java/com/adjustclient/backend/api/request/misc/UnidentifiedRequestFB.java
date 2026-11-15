package com.adjustclient.backend.api.request.misc;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UnidentifiedRequestFB extends BackendRequest {
    public UnidentifiedRequestFB(Object... fields) {
        super(GET_INVALID_BLOCKS, 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
