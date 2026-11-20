package com.adjustclient.utilities.game.server;
public record GameCountdown(String name, int countdown) {
    @Override public String toString() { return "c[name=" + name + ", countdown=" + countdown + "]"; }
}
