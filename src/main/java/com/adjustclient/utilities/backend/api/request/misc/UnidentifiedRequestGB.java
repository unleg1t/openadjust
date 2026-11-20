package com.adjustclient.utilities.backend.api.request.misc;

import com.adjustclient.utilities.backend.api.BackendRequest;
import com.adjustclient.utilities.backend.api.NullableValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class UnidentifiedRequestGB extends BackendRequest {
    public UnidentifiedRequestGB(UUID uuid, String name, byte[] data, long timestamp) {
        super("/intel/sendAnalytics2", 45000L, new ArrayList<>(Arrays.asList(
                new NullableValue(uuid),
                Objects.requireNonNull(name),
                Objects.requireNonNull(data),
                timestamp)));
    }
}
