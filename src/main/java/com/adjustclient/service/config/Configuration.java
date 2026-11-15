package com.adjustclient.service.config;

public class Configuration {
    public Object callback;
    public int requestId;
    public int kind;
    public Object field16;

    public Configuration(int requestId, int kind, Object callback) {
        this.requestId = requestId;
        this.kind = kind;
        this.callback = callback;
    }
}
