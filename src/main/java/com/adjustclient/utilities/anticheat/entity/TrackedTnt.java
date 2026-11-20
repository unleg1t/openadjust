package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.utilities.TimeUtil;
import net.minecraft.entity.item.EntityTNTPrimed;

public final class TrackedTnt extends TrackedEntity<EntityTNTPrimed> {
    public String fuseText;
    public int fuse;
    public Object field48;
    public TrackedTnt(EntityTNTPrimed entity) { super(entity); fuse = entity.fuse; }
    public boolean matchesType(int mask) { return (mask & 0x200) != 0; }
    public void update() {
        if (fuse < 1) fuseText = null;
        else { fuseText = TimeUtil.formatDuration((long) (50 * fuse), true); --fuse; }
    }
    public String getDisplayName() { return entity.getName(); }
}
