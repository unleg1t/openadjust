package com.adjustclient.service.servertracker;

public enum TrackerScope {
    STAFF_ALERT("staff alert", new java.awt.Color(0xffb69242, true)),
    BAN("ban", new java.awt.Color(0xffde465a, true));

    TrackerScope(String label, java.awt.Color color) {
        this.field16 = label;
        this.field20 = color;
    }

    public Object field16;
    public Object field20;
    public Object field24;

}
