package com.adjustclient.util.anticheat.entity;

import com.adjustclient.Adjust;
import com.adjustclient.tag.PlayerTag;
import com.adjustclient.util.Timer;
import com.adjustclient.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;

public final class TrackedFakePlayer extends TrackedLivingEntity {
    public EspPreviewData fakePreview;
    public Object field56;
    public TrackedFakePlayer(EntityPlayer entity) { super(entity); }
    public EntityPlayer getEntity() { return (EntityPlayer) entity; }
    public boolean matchesType(int mask) {
        if (entity.isUser() && (mask & 0x80) != 0) return true;
        if (entity.isInvisible() && (mask & 0x40) == 0) return false;
        boolean teammate = WorldUtil.isTeammate(entity)
                || Adjust.INSTANCE.tagManager.getTag(getDisplayName()) == PlayerTag.WHITELIST;
        if ((mask & 0x800) != 0
                && Adjust.INSTANCE.tagManager.getTag(getDisplayName()) == PlayerTag.TARGET) return true;
        if ((mask & 0x20) == 0 && teammate) return false;
        if ((mask & 0x400) != 0 && isTagged()) return false;
        return (mask & 2) != 0 || ((mask & 0x20) != 0 && teammate);
    }
    public boolean isTagged() {
        return Adjust.INSTANCE.tagManager.getTag(getDisplayName()) == PlayerTag.WHITELIST;
    }
}
