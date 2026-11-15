package com.adjustclient.util;

import com.adjustclient.setting.ModeOption;

public enum HolidayTheme implements ModeOption {
    EASTER("Easter"),
    HALLOWEEN("Halloween"),
    CHRISTMAS("Christmas");

    public final String displayName;

    HolidayTheme(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
