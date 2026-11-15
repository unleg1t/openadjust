package com.adjustclient.backend.hwid;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.util.List;
import oshi.SystemInfo;

public final class HardwareId {
    public static long cachedHash;
    public static boolean initialized;

    public static String b() {
        SystemInfo systemInfo = new SystemInfo();
        List<HwidCollector> collectors = List.of(
                new LinuxHwidCollector(systemInfo),
                new MacHwidCollector(systemInfo, false),
                new JnaHwidCollector(systemInfo, false),
                new UserEnvironmentCollector(systemInfo)
        );
        StringBuilder sb = new StringBuilder();
        for (HwidCollector collector : collectors) {
            collector.sink = new TextHwidSink(sb);
            String title;
            if (collector instanceof LinuxHwidCollector) {
                title = "Processor";
            } else if (collector instanceof JnaHwidCollector) {
                title = "Graphics Card";
            } else if (collector instanceof MacHwidCollector) {
                title = "Motherboard";
            } else {
                title = "Extra Info";
            }
            sb.append('[').append(title).append(']').append('\n');
            try {
                collector.run();
            } catch (Throwable t) {
                collector.sink.a("! THROWABLE ! " + t.toString());
            }
        }
        return sb.toString();
    }

    public static long computeHardwareId() {
        cachedHash = collectHardwareHash(false, "Failed to retrieve your hardware ID: {}/{}.");
        return cachedHash;
    }

    static long collectHardwareHash(boolean legacy, String errorFormat) {
        Hasher hasher = Hashing.sipHash24().newHasher();
        hasher.putInt(173);
        SystemInfo systemInfo = new SystemInfo();
        HwidSink sink = new HashingHwidSink(hasher);
        List<HwidCollector> collectors = List.of(
                new LinuxHwidCollector(systemInfo, sink),
                new MacHwidCollector(systemInfo, sink, legacy),
                new JnaHwidCollector(systemInfo, sink, legacy)
        );
        for (HwidCollector collector : collectors) {
            try {
                collector.run();
            } catch (Throwable ignored) {
                sink.b(0);
            }
        }
        long hash = hasher.hash().asLong();
        if (hash != 0L && hash != 32L) {
            return hash ^ 0x20L;
        }
        Logger.log(LogLevel.ERROR, true, errorFormat, hash, 3);
        System.exit(0);
        return 32L;
    }
}
