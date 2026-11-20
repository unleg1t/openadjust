package com.adjustclient.utilities.auth;

public record XboxIdentity(String identityToken) {
    @Override public String toString() {
        return "b[xbl=" + identityToken + "]";
    }
}
