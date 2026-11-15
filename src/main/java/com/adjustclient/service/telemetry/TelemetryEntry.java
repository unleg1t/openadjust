package com.adjustclient.service.telemetry;

import java.util.HashSet;
import java.util.Set;

public class TelemetryEntry {
    public final Set<String> entityNames = new HashSet<>();
    public boolean enabled;
    public boolean gameSent;
    public boolean gameStateDirty;
}
