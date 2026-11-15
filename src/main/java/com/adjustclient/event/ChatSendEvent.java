package com.adjustclient.event;

public final class ChatSendEvent extends CancellableEvent {
    public boolean addToHistory;
    public String message;

    public ChatSendEvent(String message, boolean addToHistory) {
        this.message = message; this.addToHistory = addToHistory;
    }
}
