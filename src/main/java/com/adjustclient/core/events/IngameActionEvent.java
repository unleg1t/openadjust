package com.adjustclient.core.events;

public final class IngameActionEvent extends Event {
    public boolean actionStage;
    public boolean suppressActions;

    public IngameActionEvent(boolean actionStage) {
        this.actionStage = actionStage;
    }
}
