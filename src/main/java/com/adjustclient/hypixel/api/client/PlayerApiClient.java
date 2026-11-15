package com.adjustclient.hypixel.api.client;

import com.adjustclient.hypixel.api.model.ApiResult;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import java.util.UUID;
import java.io.IOException;
import com.adjustclient.hypixel.api.model.status.HypixelStatusResponse;

public abstract class PlayerApiClient extends ApiClient {
    protected PlayerApiClient(long windowMillis, int requestLimit) { super(windowMillis, requestLimit); }
    public abstract ApiResult<HypixelPlayer> fetchPlayer(UUID uuid) throws IOException;

    public HypixelStatusResponse fetchStatus(String uuid) { return null; }
}
