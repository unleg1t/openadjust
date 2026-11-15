package com.adjustclient.backend.hwid;

import oshi.SystemInfo;

public abstract class HwidCollector implements Runnable {
    protected final SystemInfo systemInfo;
    public HwidSink sink;

    protected HwidCollector(SystemInfo systemInfo, HwidSink sink) {
        this.systemInfo = systemInfo;
        this.sink = sink;
    }

    protected HwidCollector(SystemInfo systemInfo) {
        this(systemInfo, null);
    }
}
