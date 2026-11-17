package com.adjustclient.event;

import com.adjustclient.game.rewound.RewoundDisablerType;

public final class RewoundDisablerEvent extends Event {
    public final RewoundDisablerType type;

    public RewoundDisablerEvent(RewoundDisablerType type) {
        this.type = type;
    }
}
