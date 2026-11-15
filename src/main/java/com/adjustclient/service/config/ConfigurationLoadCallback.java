package com.adjustclient.service.config;

@FunctionalInterface
public interface ConfigurationLoadCallback {
    void onReply(int status, byte[] data);
}
