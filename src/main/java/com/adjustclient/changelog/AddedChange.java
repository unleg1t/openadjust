package com.adjustclient.changelog;

public class AddedChange extends ChangelogChange {
    public AddedChange(String text) {
        super(text, "+");
    }
}
