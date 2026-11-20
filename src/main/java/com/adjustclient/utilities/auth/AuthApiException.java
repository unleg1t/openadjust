package com.adjustclient.utilities.auth;

import com.adjustclient.utilities.http.ApiException;

public final class AuthApiException extends ApiException {
    public AuthApiException() { super("No Minecraft profile!"); }
}
