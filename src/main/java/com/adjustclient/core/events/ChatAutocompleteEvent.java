package com.adjustclient.core.events;

public final class ChatAutocompleteEvent extends Event {
    public String text;
    public String word;
    public String[] completions;

    public ChatAutocompleteEvent(String text, String word) {
        this.text = text;
        this.word = word;
    }
}
