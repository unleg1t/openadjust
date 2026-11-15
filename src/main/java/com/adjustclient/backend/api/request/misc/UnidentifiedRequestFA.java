package com.adjustclient.backend.api.request.misc;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class UnidentifiedRequestFA extends BackendRequest {
    public UnidentifiedRequestFA(Object... fields) {
        super("/hypixel/addInvalidBlock", 45000L,
                new ArrayList<>(Arrays.asList(fields)));
    }
}
