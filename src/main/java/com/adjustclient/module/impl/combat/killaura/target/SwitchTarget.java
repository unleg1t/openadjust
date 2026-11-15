package com.adjustclient.module.impl.combat.killaura.target;

import com.adjustclient.util.Timer;

public class SwitchTarget {
    public final Timer timer = new Timer();
    public final int entityId;
    public boolean attacked;
    public SwitchTarget(int entityId) { this.entityId = entityId; }
}
