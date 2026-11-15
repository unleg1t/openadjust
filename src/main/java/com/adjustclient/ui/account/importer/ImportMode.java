package com.adjustclient.ui.account.importer;

public enum ImportMode {
    MICROSOFT("Microsoft Credentials"), COOKIE("Cookie files"), REFRESH("Refresh tokens");
    public final String displayName;

    ImportMode(String displayName) { this.displayName = displayName; }
}
