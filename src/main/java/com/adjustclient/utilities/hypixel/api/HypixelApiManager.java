package com.adjustclient.utilities.hypixel.api;

import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.hypixel.api.client.AntiSniperApiClient;
import com.adjustclient.utilities.hypixel.api.client.CubelifyApiClient;
import com.adjustclient.utilities.hypixel.api.client.EventTimerFetcher;
import com.adjustclient.utilities.hypixel.api.client.HypixelApiClient;
import com.adjustclient.utilities.hypixel.api.client.PlayerApiClient;
import com.adjustclient.utilities.hypixel.api.client.SeraphApiClient;
import com.adjustclient.utilities.hypixel.api.client.UrchinApiClient;
import com.adjustclient.utilities.hypixel.api.client.karma.TwentyFiveKarmaApiClient;
import com.adjustclient.utilities.hypixel.api.model.ApiResult;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.status.HypixelStatusResponse;
import com.adjustclient.managers.config.user.UserConfiguration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public final class HypixelApiManager implements Listener {
    public final HypixelApiClient hypixelClient;
    public final UrchinApiClient urchinClient;
    public final SeraphApiClient seraphClient;
    public final AntiSniperApiClient antiSniperClient;
    public final CubelifyApiClient cubelifyClient;
    public final TwentyFiveKarmaApiClient karmaClient;
    public final EventTimerFetcher eventTimerFetcher;
    public final PlayerApiClient[] playerProviders;
    public final Map<UUID, HypixelPlayer> playerCache;
    public final Map<UUID, CompletableFuture<HypixelPlayer>> pendingRequests;
    private byte tickCounter;
    private Object field48;

    public HypixelApiManager(TwentyFiveKarmaApiClient karmaClient) {
        this.karmaClient = karmaClient;
        hypixelClient = new HypixelApiClient();
        urchinClient = new UrchinApiClient();
        seraphClient = new SeraphApiClient();
        antiSniperClient = new AntiSniperApiClient();
        cubelifyClient = new CubelifyApiClient();
        eventTimerFetcher = new EventTimerFetcher();
        playerProviders = new PlayerApiClient[] {hypixelClient, cubelifyClient, antiSniperClient, karmaClient};
        playerCache = new HashMap<>();
        pendingRequests = new HashMap<>();
    }

    public void tick() {
        if (tickCounter++ != 10) {
            return;
        }
        synchronized (playerCache) {
            playerCache.values().removeIf(player ->
                    player.fetchedAt + 60000L <= System.currentTimeMillis());
        }
        for (PlayerApiClient provider : playerProviders) {
            provider.pruneRequests();
        }
        tickCounter = 0;
    }

    public boolean isUrchinAvailable() {
        return UserConfiguration.URCHIN_API_KEY.keyString != null
                && urchinClient.requests.size() < urchinClient.requestLimit;
    }

    public boolean isAntiSniperAvailable() {
        return UserConfiguration.ANTI_SNIPER_API_KEY.keyString != null
                && antiSniperClient.requests.size() < antiSniperClient.requestLimit;
    }

    @com.adjustclient.core.events.EventHandler
    public void onWorldReset(com.adjustclient.core.events.WorldResetEvent event) { reset(); }

    public void reset() {
        synchronized (antiSniperClient.blacklistRequests) {
            antiSniperClient.blacklistRequests.clear();
        }
        synchronized (antiSniperClient.winstreakRequests) {
            antiSniperClient.winstreakRequests.clear();
        }
        synchronized (seraphClient.requestsByPlayer) {
            seraphClient.requestsByPlayer.clear();
        }
        synchronized (pendingRequests) {
            pendingRequests.clear();
        }
    }

    public void removePending(UUID uuid, CompletableFuture<HypixelPlayer> future) {
        synchronized (pendingRequests) {
            if (pendingRequests.get(uuid) == future) {
                pendingRequests.remove(uuid);
            }
        }
    }

    public boolean isSeraphAvailable() {
        return UserConfiguration.SERAPH_API_KEY.keyString != null
                && seraphClient.requests.size() < seraphClient.requestLimit;
    }

    public CompletableFuture<HypixelPlayer> getPlayer(UUID uuid) {
        HypixelPlayer cached = getCachedPlayer(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return fetchPlayer(uuid);
    }

    public boolean isHypixelAvailable() {
        for (PlayerApiClient provider : playerProviders) {
            if (provider.requests.size() >= provider.requestLimit) {
                continue;
            }
            if (provider == hypixelClient) {
                if (UserConfiguration.HYPIXEL_API_KEY.keyString == null) {
                    continue;
                }
            } else if (provider == antiSniperClient && !isAntiSniperAvailable()) {
                continue;
            }
            return true;
        }
        return false;
    }

    public HypixelPlayer getCachedPlayer(UUID uuid) {
        synchronized (playerCache) {
            return playerCache.get(uuid);
        }
    }

    public CompletableFuture<HypixelPlayer> fetchPlayer(UUID uuid) {
        synchronized (pendingRequests) {
            CompletableFuture<HypixelPlayer> existing = pendingRequests.get(uuid);
            if (existing != null) {
                return existing;
            }
            CompletableFuture<HypixelPlayer> future = CompletableFuture.supplyAsync(() -> {
                for (PlayerApiClient provider : playerProviders) {
                    if (provider.requests.size() >= provider.requestLimit) {
                        continue;
                    }
                    if (provider == hypixelClient) {
                        if (UserConfiguration.HYPIXEL_API_KEY.keyString == null) {
                            continue;
                        }
                    } else if (provider == antiSniperClient && !isAntiSniperAvailable()) {
                        continue;
                    }
                    ApiResult<HypixelPlayer> result;
                    try {
                        result = provider.fetchPlayer(uuid);
                    } catch (IOException exception) {
                        // CompletableFuture wraps exceptions thrown by its supplier.
                        throw new CompletionException(exception);
                    }
                    if (result == null) {
                        continue;
                    }
                    if (!result.success()) {
                        return null;
                    }
                    HypixelPlayer player = result.response();
                    if (player != null) {
                        synchronized (playerCache) {
                            playerCache.put(uuid, player);
                        }
                    }
                    return player;
                }
                return null;
            });
            pendingRequests.put(uuid, future);
            future.whenComplete((player, error) -> {
                removePending(uuid, future);
            });
            return future;
        }
    }

    public Supplier<HypixelStatusResponse> statusTask(String uuid) {
        return () -> {
            for (PlayerApiClient provider : playerProviders) {
                if (provider.requests.size() >= provider.requestLimit) {
                    continue;
                }
                if (provider == hypixelClient) {
                    if (UserConfiguration.HYPIXEL_API_KEY.keyString == null) {
                        continue;
                    }
                } else if (provider == antiSniperClient && !isAntiSniperAvailable()) {
                    continue;
                }
                HypixelStatusResponse status = provider.fetchStatus(uuid);
                if (status != null) {
                    return status;
                }
            }
            return null;
        };
    }
}
