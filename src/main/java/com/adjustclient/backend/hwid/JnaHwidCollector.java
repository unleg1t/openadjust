package com.adjustclient.backend.hwid;

import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

public class JnaHwidCollector extends HwidCollector {
    private final boolean legacy;

    public JnaHwidCollector(SystemInfo systemInfo, HwidSink sink, boolean legacy) {
        super(systemInfo, sink);
        this.legacy = legacy;
    }

    public JnaHwidCollector(SystemInfo systemInfo, boolean legacy) {
        super(systemInfo);
        this.legacy = legacy;
    }

    @Override
    public void run() {
        if (this.legacy) {
            throw new IllegalArgumentException(
                    "Can't create an instance of class com.sun.jna.ptr.LongByReference, requires a public no-arg constructor: java.lang.NoSuchMethodException: com.sun.jna.ptr.LongByReference.<init>()");
        }
        List<GraphicsCard> cards = this.systemInfo.getHardware().getGraphicsCards();
        if (cards.isEmpty()) {
            throw new RuntimeException();
        }
        for (GraphicsCard card : cards) {
            this.sink.a(card.getVendor());
            this.sink.a(card.getName());
            this.sink.a(card.getDeviceId());
        }
    }
}
