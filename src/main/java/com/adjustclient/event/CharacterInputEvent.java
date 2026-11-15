package com.adjustclient.event;

public final class CharacterInputEvent extends Event {
    public char character;

    public CharacterInputEvent(char character) {
        this.character = character;
    }
}
