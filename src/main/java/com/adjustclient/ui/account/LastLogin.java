package com.adjustclient.ui.account;

public final class LastLogin {

    final String user;
    final long time;
    final Object obf16;

    public LastLogin(String user, long time, Object obf16) {
        this.user = user;
        this.time = time;
        this.obf16 = obf16;
    }
}
