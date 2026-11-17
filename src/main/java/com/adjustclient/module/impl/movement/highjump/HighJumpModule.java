package com.adjustclient.module.impl.movement.highjump;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.WorldUtil;

public final class HighJumpModule extends Module {
    public static HighJumpModule INSTANCE;
    public final ModeSetting<HighJumpMode> mode;
    public final NumberSetting height;
    public final NumberSetting maxJumps;
    public final BooleanSetting voidOnly;
    public final Bind holdBind;
    public int jumps;

    public HighJumpModule() {
        super("High Jump", Category.MOVEMENT);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", HighJumpMode.REWOUND, HighJumpMode.values());
        height = new NumberSetting(this, "Height", 1.0D, 0.5D, 5.0D, 0.01D);
        maxJumps = new NumberSetting(this, "Max. jumps", 5, 1, 5, 1)
                .formatter(setting -> setting.getInt() == setting.getMax().intValue()
                        ? "Unlimited"
                        : Integer.toString(setting.getInt()));
        voidOnly = new BooleanSetting(this, "Void only", true, "On", "Off");
        holdBind = Adjust.INSTANCE.bindManager.register("highjump.optionalbind", bindMode -> {
        }, "High Jump Bind");
        new KeybindSetting(this, "Hold Bind", holdBind);
    }

    public boolean canJump() {
        if (!isEnabled()) return false;
        if (!holdBind.isHeld()) return false;
        int max = maxJumps.getMax().intValue();
        int configured = maxJumps.getInt();
        if (configured != max && jumps >= configured) return false;
        if (voidOnly.isEnabled() && !voidOnly.isHidden()) {
            return WorldUtil.isOverVoid();
        }
        return true;
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || mc.thePlayer == null) return;
        if (mc.thePlayer.onGround) {
            jumps = 0;
            return;
        }
        if (!canJump()) return;
        double jumpHeight = height.getDouble();
        if (mc.thePlayer.lastGroundY - mc.thePlayer.posY > jumpHeight + 3.0D) {
            mc.thePlayer.motionY = jumpHeight;
            ++jumps;
        }
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }
}
