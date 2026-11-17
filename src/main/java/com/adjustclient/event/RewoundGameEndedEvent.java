package com.adjustclient.event;

import com.adjustclient.game.rewound.RewoundGameState;

public final class RewoundGameEndedEvent extends Event {
    public final RewoundGameState state;

    public RewoundGameEndedEvent(RewoundGameState state) {
        this.state = state;
    }
}
