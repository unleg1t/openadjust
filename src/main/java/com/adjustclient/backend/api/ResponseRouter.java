package com.adjustclient.backend.api;

import com.adjustclient.backend.api.response.*;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.BackendMessageHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import com.adjustclient.backend.api.response.ResponseStatus;

public class ResponseRouter implements BackendMessageHandler {
    public final Long2ObjectOpenHashMap<PendingRequest> pending = new Long2ObjectOpenHashMap<>();

    public void onDisconnect() {
        PendingRequest[] requests;
        synchronized (pending) {
            requests = pending.values().toArray(new PendingRequest[0]);
            pending.clear();
        }
        for (PendingRequest request : requests) {
            request.complete(new BackendResponse(request, ResponseStatus.CONNECTION_RESET,
                    EmptyResponseBody.INSTANCE));
        }
    }

    public void handleResponse(long requestId, ResponseStatus status, ResponseBody body) {
        PendingRequest request;
        synchronized (pending) {
            request = pending.get(requestId);
            if (request == null) {
                Logger.log(LogLevel.WARN, true, "Got API response {} to unknown request", requestId);
                return;
            }
            pending.remove(requestId);
        }
        request.complete(new BackendResponse(request, status, body));
    }

    public void onConnect() {
        synchronized (pending) { pending.clear(); }
    }

    public void expireRequests() {
        List<PendingRequest> expired = null;
        synchronized (pending) {
            for (PendingRequest request : pending.values()) {
                if (request.timeoutMs >= 1 && request.timer != null
                        && request.timer.lastMs + request.timeoutMs <= System.currentTimeMillis()) {
                    if (expired == null) expired = new ArrayList<>(1);
                    expired.add(request);
                }
            }
        }
        if (expired != null) {
            for (PendingRequest request : expired) {
                handleResponse(request.requestId, ResponseStatus.TIMED_OUT,
                        EmptyResponseBody.INSTANCE);
            }
        }
    }

    public void register(PendingRequest request) {
        synchronized (pending) { pending.put(request.requestId, request); }
    }
}
