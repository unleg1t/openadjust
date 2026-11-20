package com.adjustclient.core.gui.account;

public enum AccountType {

    COOKIE("Cookie"),
    CREDENTIALS("Credentials"),
    SESSION("Session"),
    BROWSER("Browser");

    public final String displayName;
    private Object obf20;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

}
