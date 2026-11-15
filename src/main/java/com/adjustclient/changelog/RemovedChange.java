package com.adjustclient.changelog;

public class RemovedChange extends ChangelogChange {
    public RemovedChange(String text) {
        super(text, "-");
    }
}
