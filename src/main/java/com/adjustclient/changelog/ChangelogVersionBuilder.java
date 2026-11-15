package com.adjustclient.changelog;

import java.util.ArrayList;
import java.util.List;

public class ChangelogVersionBuilder {
    public List<ChangelogChange> changes = new ArrayList<>();
    public Object field8;

    public ChangelogVersionBuilder add(String text) {
        changes.add(new AddedChange(text));
        return this;
    }

    public ChangelogVersionBuilder remove(String text) {
        changes.add(new RemovedChange(text));
        return this;
    }

    public ChangelogVersionBuilder improve(String text) {
        changes.add(new ImprovedChange(text));
        return this;
    }
}
