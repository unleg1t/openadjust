package com.adjustclient.auth;

public record Cookie(String name, String value) {
    @Override public String toString() { return name + "=" + value; }
}
