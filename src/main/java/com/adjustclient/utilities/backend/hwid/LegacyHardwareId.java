package com.adjustclient.utilities.backend.hwid;

public final class LegacyHardwareId {
    private static long cachedHash;

    public static long d() {
        cachedHash = HardwareId.collectHardwareHash(true, "Failed to retrieve your legacy hardware ID: {}/{}.");
        return cachedHash;
    }
}
