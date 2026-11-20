package com.adjustclient.managers.config;

@FunctionalInterface
public interface ConfigurationListener {
    void onReply(int status);
}
