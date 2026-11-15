package com.adjustclient.backend.api.request.client;

import com.adjustclient.backend.api.BackendRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class LinkDiscordRequest extends BackendRequest {
    public LinkDiscordRequest() {
        super("/client/linkDiscord", 45000L, new ArrayList<>(Arrays.asList()));
    }
}
