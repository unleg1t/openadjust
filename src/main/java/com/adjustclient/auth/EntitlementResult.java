package com.adjustclient.auth;

public record EntitlementResult(boolean valid) {
    @Override public String toString() {
        return "b[owned=" + valid + "]";
    }
}
