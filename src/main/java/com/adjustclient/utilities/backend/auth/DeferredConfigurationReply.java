package com.adjustclient.utilities.backend.auth;

public class DeferredConfigurationReply {
    public final byte[] data;
    public final int requestId;
    public final int status;
    public final int hash;

    public DeferredConfigurationReply(byte[] data, int requestId, int status, int hash) {
        this.data = data;
        this.requestId = requestId;
        this.status = status;
        this.hash = hash;
    }

    // Preserve the argument order already used by ConfigurationService.receiveData.
    public DeferredConfigurationReply(int requestId, int status, int hash, byte[] data) {
        this(data, requestId, status, hash);
    }
}
