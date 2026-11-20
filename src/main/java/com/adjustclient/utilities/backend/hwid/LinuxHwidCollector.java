package com.adjustclient.utilities.backend.hwid;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class LinuxHwidCollector extends HwidCollector {
    public LinuxHwidCollector(SystemInfo systemInfo, HwidSink sink) {
        super(systemInfo, sink);
    }

    public LinuxHwidCollector(SystemInfo systemInfo) {
        super(systemInfo);
    }

    @Override
    public void run() {
        try {
            CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
            this.sink.a(processor.getPhysicalPackageCount());
            this.sink.a(processor.getLogicalProcessorCount());
        } catch (Throwable ignored) {
            this.sink.b(5);
        }
        try {
            CentralProcessor.ProcessorIdentifier identifier =
                    this.systemInfo.getHardware().getProcessor().getProcessorIdentifier();
            this.sink.a(identifier.getIdentifier());
            this.sink.a(identifier.getProcessorID());
            this.sink.a(identifier.getFamily());
            this.sink.a(identifier.getName());
            this.sink.a(identifier.getModel());
            this.sink.a(identifier.getMicroarchitecture());
            this.sink.a(identifier.getVendor());
        } catch (Throwable ignored) {
            this.sink.b(4);
        }
    }
}
