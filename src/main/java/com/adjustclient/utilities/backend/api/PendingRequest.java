package com.adjustclient.utilities.backend.api;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.response.*;
import com.adjustclient.utilities.backend.transport.HydraPacketEncoder;
import com.adjustclient.utilities.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;

public class PendingRequest {
    public BackendRequest request;
    public byte[] body;
    public CompletableFuture<BackendResponse> future;
    public Consumer<BackendResponse> callback;
    public final Object lock = new Object();
    public Timer timer;
    public long requestId;
    public long timeoutMs;

    public PendingRequest(BackendRequest request, byte[] body, long requestId, long timeoutMs) {
        this.request = request;
        this.body = body;
        this.requestId = requestId;
        this.timeoutMs = timeoutMs;
    }

    public void complete(BackendResponse response) {
        Consumer<BackendResponse> handler;
        synchronized (lock) {
            future.complete(response);
            handler = callback;
        }
        if (handler != null) handler.accept(response);
    }

    public CompletableFuture<BackendResponse> sendAsync(Consumer<BackendResponse> handler) {
        boolean first;
        synchronized (lock) {
            if (future != null && callback != null) return future;
            first = future == null;
            if (first) future = new CompletableFuture<>();
            if (callback == null) callback = handler;
        }
        if (first && !trySend()) {
            complete(new BackendResponse(this, ResponseStatus.DISCONNECTED, EmptyResponseBody.INSTANCE));
        }
        return future;
    }

    public boolean trySend() {
        if (Adjust.hydraConnection == null || !Adjust.hydraConnection.authenticated
                || !Adjust.hydraConnection.ready) return false;
        ResponseRouter router = Adjust.INSTANCE.screenStack.responses;
        router.register(this);
        byte[] packet = HydraPacketEncoder.encodeApiRequest(request.endpointHash, requestId, body);
        com.adjustclient.managers.util.ServiceUtil.sendToBackend(packet);
        timer = new Timer();
        return true;
    }
}
