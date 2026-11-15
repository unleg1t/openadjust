package com.adjustclient.service.config;

@FunctionalInterface
public interface ConfigurationListener {
    void onReply(int status);
}
