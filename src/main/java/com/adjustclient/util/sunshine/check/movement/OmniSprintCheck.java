package com.adjustclient.util.sunshine.check.movement;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.MovementListener;
import com.adjustclient.util.sunshine.tracker.TrackedPosition;
import net.minecraft.util.MathHelper;
import com.adjustclient.util.sunshine.CheckCategory;
public final class OmniSprintCheck extends SunshineCheck implements MovementListener {
    public int backwardsTicks;
    public Object field24;
    public OmniSprintCheck() { super("Omnidirectional Sprint", "Detects omnidirectional sprinting on ground"); }
    @Override public void onMovement(TrackedPosition from, TrackedPosition to) {
        if (to.teleport || from==null || !from.ground || !to.ground) return;
        double dx=to.xCoord-from.xCoord, dz=to.zCoord-from.zCoord;
        float direction=(float)(Math.atan2(-dx,dz)*57.29577951308232);
        float difference=Math.abs(MathHelper.wrapAngleTo180_float(direction-to.rotation.yaw));
        if (difference<135.0F) { backwardsTicks=0; return; }
        double speed=MathHelper.sqrt_double(dx*dx+dz*dz);
        if (speed<=MovementUtil.getSpeedWithPotion(0.221,0.1)) backwardsTicks=0;
        else if (++backwardsTicks>=11)
            fail(new ViolationDetails(CheckCategory.ALERT).put("speed",speed).put("difference",difference));
    }
}
