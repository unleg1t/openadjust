package com.adjustclient.utilities.backend.api.response;

public enum ResponseStatus {
    OK, UNAUTHORIZED, INVALID_PARAMETER, BAD_ENDPOINT, ERROR, RATELIMITED,
    TIMED_OUT, DISCONNECTED, CONNECTION_RESET;
    public Object field16;
    @Override public String toString() { return name(); }
}
