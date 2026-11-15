package com.adjustclient.auth;

import com.adjustclient.util.http.ApiException;

public final class AuthApiException extends ApiException {
    public AuthApiException() { super("No Minecraft profile!"); }
}
