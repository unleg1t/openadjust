package com.adjustclient.managers.config;

@FunctionalInterface
public interface ConfigurationLoadCallback {
    void onReply(int status, byte[] data);
}
