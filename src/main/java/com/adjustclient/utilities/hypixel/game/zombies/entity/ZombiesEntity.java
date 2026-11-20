package com.adjustclient.utilities.hypixel.game.zombies.entity;

import com.adjustclient.utilities.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.utilities.hypixel.game.zombies.ZombiesPowerUp;

public final class ZombiesEntity extends TrackedEntityRef {
    public final ZombiesPowerUp powerUp;
    public Object field16;
    public ZombiesEntity(ZombiesGameState state, int id, ZombiesPowerUp powerUp) {
        super(id); this.state = state; this.powerUp = powerUp;
    }
    @Override public void update() { }
    @Override public boolean isExpired() { return getEntity() == null; }
}
