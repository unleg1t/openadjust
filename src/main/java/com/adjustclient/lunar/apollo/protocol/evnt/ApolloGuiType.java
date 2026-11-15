package com.adjustclient.lunar.apollo.protocol.evnt;

public enum ApolloGuiType {
    GUI_TYPE_UNSPECIFIED(0),
    GUI_TYPE_SUIT(0),
    GUI_TYPE_CHARACTER_SELECTION(0),
    GUI_TYPE_OBSERVER_UI(0),
    GUI_TYPE_WELCOME_UI(0),
    GUI_TYPE_ESC(0),
    GUI_TYPE_PLAYER_INVENTORY(0),
    UNRECOGNIZED(0);

    public final int number;
    public final Object field16 = null;

    ApolloGuiType(int number) {
        this.number = number;
    }
}
