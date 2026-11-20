package com.adjustclient.core.events;

import com.adjustclient.utilities.game.rewound.RewoundGameState;

public final class RewoundGameEndedEvent extends Event {
    public final RewoundGameState state;

    public RewoundGameEndedEvent(RewoundGameState state) {
        this.state = state;
    }
}
