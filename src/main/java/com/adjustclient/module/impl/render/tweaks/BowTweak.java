package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;

public final class BowTweak extends Tweak {
    public static BowTweak INSTANCE;
    public final BooleanSetting leftClickShoot;
    public final BooleanSetting onFullChargeOnly;

    public BowTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(module, "Bow");
        group.enabledPredicate = this::isLeftClickShootEnabled;
        leftClickShoot = new BooleanSetting(group, "Left click shoot", false);
        onFullChargeOnly = new BooleanSetting(group, "On full charge only", false,
                () -> !leftClickShoot.isEnabled());
    }

    private boolean isLeftClickShootEnabled() {
        return leftClickShoot.isEnabled();
    }

    @EventHandler(priority = 75)
    public void onTick(IngameActionEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        ItemStack inUse = mc.thePlayer.getItemInUse();
        if (inUse == null || inUse.getItem() == null || inUse.getItem().getClass() != ItemBow.class) return;
        var left = Adjust.mouseClickHelper.left;
        if (onFullChargeOnly.isEnabled()) {
            if (inUse.getMaxItemUseDuration() - mc.thePlayer.getItemInUseCount() < 20) return;
            if (!left.isPressed(false) && !left.isClicked(false)) return;
        } else {
            if (!left.isPressed(false)) return;
        }
        Adjust.mouseClickHelper.right.disabled = true;
        if (!mc.thePlayer.isSwingInProgress
                || mc.thePlayer.swingProgressInt >= mc.thePlayer.getArmSwingAnimationEnd() / 2
                || mc.thePlayer.swingProgressInt < 0) {
            mc.thePlayer.swingProgressInt = -1;
            mc.thePlayer.isSwingInProgress = true;
        }
    }

    @Override
    protected boolean enabledSetting() {
        return leftClickShoot.isEnabled();
    }
}
