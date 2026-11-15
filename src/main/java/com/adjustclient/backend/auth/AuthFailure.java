package com.adjustclient.backend.auth;

public class AuthFailure extends Throwable {
    public Object field28;

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException fail() throws T {
        throw (T) new AuthFailure();
    }
}
