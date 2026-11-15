package com.adjustclient.backend.hwid;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;

public class MacHwidCollector extends HwidCollector {
    public static boolean appleDetected;
    private final boolean legacy;

    public MacHwidCollector(SystemInfo systemInfo, HwidSink sink, boolean legacy) {
        super(systemInfo, sink);
        this.legacy = legacy;
    }

    public MacHwidCollector(SystemInfo systemInfo, boolean legacy) {
        super(systemInfo);
        this.legacy = legacy;
    }

    private boolean b(String value) {
        return value != null && value.trim().equals("Apple Inc.");
    }

    @Override
    public void run() {
        try {
            ComputerSystem computerSystem = this.systemInfo.getHardware().getComputerSystem();
            try {
                String model = computerSystem.getModel();
                if (model != null && model.startsWith("Mac") && model.length() < 11 && model.contains(",")) {
                    appleDetected = true;
                }
                this.sink.a(model);
                this.sink.a(computerSystem.getSerialNumber());
                this.sink.a(computerSystem.getManufacturer());
            } catch (Throwable ignored) {
                this.sink.b(1);
            }
            try {
                Baseboard baseboard = computerSystem.getBaseboard();
                String manufacturer = baseboard.getManufacturer();
                if (!appleDetected && b(manufacturer)) {
                    appleDetected = true;
                }
                this.sink.a(manufacturer);
                this.sink.a(baseboard.getSerialNumber());
                this.sink.a(this.legacy ? "unknown" : baseboard.getModel());
            } catch (Throwable ignored) {
                this.sink.b(2);
            }
        } catch (Throwable ignored) {
            this.sink.b(3);
        }
    }
}
