package com.adjustclient.module.impl.player.fastuse.feature;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.module.impl.player.fastuse.FastUseFeature;
import com.adjustclient.module.impl.player.fastuse.FastUseModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class EatingFeature extends FastUseFeature {
    public static EatingFeature INSTANCE;
    public final BooleanSetting enabled;
    public final ModeSetting<FastUseModule.EatingMode> mode;
    public boolean burstSent;

    public EatingFeature(FastUseModule module) {
        super(module);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(module, "Eating");
        group.enabledPredicate = () -> isFeatureEnabled();
        enabled = new BooleanSetting(group, "Enabled", false, "Yes", "No");
        mode = new ModeSetting<>(group, "Mode", FastUseModule.EatingMode.VANILLA, FastUseModule.EatingMode.values());
    }

    @Override public void onDisable() {
        burstSent = false;
    }

    @EventHandler(priority = -4) public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre || mode.getValue() != FastUseModule.EatingMode.VANILLA) return;
        if (!isEating()) {
            burstSent = false;
            return;
        }
        if (burstSent) return;
        for (int i = 0; i < mc.thePlayer.getItemInUse().getMaxItemUseDuration() - 1; i++) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
        }
        burstSent = true;
    }

    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    public FastUseModule.EatingMode getActiveMode() { return enabled.isEnabled() ? mode.getValue() : null; }

    public static boolean isEating() {
        if (mc.thePlayer.getItemInUse() == null) return false;
        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack == null) return false;
        Item item = stack.getItem();
        return item instanceof ItemFood || item != null && (item.getClass() == ItemBucketMilk.class
                || item.getClass() == ItemPotion.class && !ItemPotion.isSplash(stack.getMetadata()));
    }
}
