package com.adjustclient.utilities.backend.api.request.client;

import com.adjustclient.utilities.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class SetTagDataRequest extends BackendRequest {
    public SetTagDataRequest(byte[] data) {
        super("/client/setTagData", 45000L, new ArrayList<>(Arrays.asList(data)));
    }
}
