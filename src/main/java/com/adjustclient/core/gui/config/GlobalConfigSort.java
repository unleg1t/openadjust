package com.adjustclient.core.gui.config;

import java.util.Comparator;

enum GlobalConfigSort {
    DOWNLOADS("Downloads", Comparator.comparingInt(
            (GlobalConfigEntry entry) -> entry.downloads).reversed()),
    VOTES("Votes", Comparator.comparingInt(
            (GlobalConfigEntry entry) -> entry.upvotes - entry.downvotes).reversed()),
    LAST_UPDATED("Last updated", Comparator.comparingLong(
            (GlobalConfigEntry entry) -> entry.updatedAt).reversed()),
    CREATION_DATE("Creation date", Comparator.comparingLong(
            (GlobalConfigEntry entry) -> entry.createdAt).reversed());

    public final String displayName;
    public final Comparator<GlobalConfigEntry> comparator;
    private Object field24;

    GlobalConfigSort(String displayName, Comparator<GlobalConfigEntry> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

}
