package com.adjustclient.modules.impl.combat.killaura.target;

import com.adjustclient.utilities.Timer;

public class SwitchTarget {
    public final Timer timer = new Timer();
    public final int entityId;
    public boolean attacked;
    public SwitchTarget(int entityId) { this.entityId = entityId; }
}
