package com.adjustclient.utilities;

import com.adjustclient.core.settings.ModeOption;

public enum HolidayTheme implements ModeOption {
    EASTER("Easter"),
    HALLOWEEN("Halloween"),
    CHRISTMAS("Christmas");

    public final String displayName;

    HolidayTheme(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
