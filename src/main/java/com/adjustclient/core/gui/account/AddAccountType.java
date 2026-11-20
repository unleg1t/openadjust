package com.adjustclient.core.gui.account;

public enum AddAccountType {
    MICROSOFT("Microsoft"), COOKIE("Cookie file"), BROWSER("Browser"),
    SESSION("Session"), REFRESH("Refresh");

    public final String displayName;
    AddAccountType(String displayName) { this.displayName = displayName; }

}
