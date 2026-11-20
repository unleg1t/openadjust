package com.adjustclient.utilities.placement;

public final class PlacementRecord {
    public Object source;
    public String reason;
    public String detail;
    public double weight;
    public Object field24;
    public PlacementRecord(Object source, String reason, String detail, double weight) {
        this.source = source; this.reason = reason; this.detail = detail; this.weight = weight;
    }
}
