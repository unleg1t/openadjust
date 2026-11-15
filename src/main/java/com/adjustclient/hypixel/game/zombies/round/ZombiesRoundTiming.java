package com.adjustclient.hypixel.game.zombies.round;

import java.util.Objects;

public record ZombiesRoundTiming(int ticks, ZombiesBoss normalTag, ZombiesBoss hardTag, ZombiesBoss ripTag) {
    @Override public boolean equals(Object other) {
        return this == other || other instanceof ZombiesRoundTiming t && ticks == t.ticks
            && normalTag == t.normalTag && hardTag == t.hardTag && ripTag == t.ripTag;
    }
    @Override public int hashCode() {
        return 31 * (31 * (31 * ticks + Objects.hashCode(normalTag)) + Objects.hashCode(hardTag)) + Objects.hashCode(ripTag);
    }
    @Override public String toString() {
        return "d[ticks=" + ticks + ", normalTag=" + normalTag + ", hardTag=" + hardTag + ", ripTag=" + ripTag + "]";
    }
}
