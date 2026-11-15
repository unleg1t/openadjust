package com.adjustclient.event;

public final class UserInputVerificationEvent extends Event {
    public boolean inputRequested;
    public boolean inputHandled;

    public UserInputVerificationEvent(boolean inputRequested) {
        this.inputRequested = inputRequested;
    }
}
