package com.adjustclient.core.events;

import com.adjustclient.utilities.game.rewound.RewoundDisablerType;

public final class RewoundDisablerEvent extends Event {
    public final RewoundDisablerType type;

    public RewoundDisablerEvent(RewoundDisablerType type) {
        this.type = type;
    }
}
