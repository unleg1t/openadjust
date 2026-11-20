package com.adjustclient.core.events;

public final class UserInputVerificationEvent extends Event {
    public Boolean serverInventoryOpen;
    public boolean inputRequested;
    public boolean inputHandled;

    public UserInputVerificationEvent(boolean inputRequested) {
        this.inputRequested = inputRequested;
    }
}
