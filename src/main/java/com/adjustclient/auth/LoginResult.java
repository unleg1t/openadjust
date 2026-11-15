package com.adjustclient.auth;

import net.minecraft.util.Session;

public final class LoginResult {
    public final Session session;
    public final LoginStatus status;
    public LoginResult(Session session, LoginStatus status) {
        this.session = session;
        this.status = status;
    }
    public LoginResult(LoginStatus status) { this(null, status); }
}
