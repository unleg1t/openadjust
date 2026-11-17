package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ModeSetting;
import java.util.Objects;
import net.minecraft.item.*;

public final class NoSlowdownModule extends Module {
    public static NoSlowdownModule INSTANCE;
    public final ModeSetting<NoSlowMode> mode;
    public final BooleanSetting clickWhileUsing;

    public NoSlowdownModule() {
        super("No Slowdown", Category.MOVEMENT);
        INSTANCE = this;
        clickWhileUsing = new BooleanSetting(this, "Click while using", true);
        clickWhileUsing.setDescription("Allows you to swing your item while you're blocking, eating or drinking.");
        VanillaNoSlowMode vanilla = new VanillaNoSlowMode(this, Features.NO_SLOW_VANILLA);
        CustomNoSlowMode custom = new CustomNoSlowMode(this);
        HypixelNoSlowMode hypixel = new HypixelNoSlowMode(this, Features.NO_SLOW_HYPIXEL);
        RewoundNoSlowMode rewound = new RewoundNoSlowMode(this, Features.NO_SLOW_REWOUND);
        SwapNoSlowMode swap = new SwapNoSlowMode(this);
        LegitNoSlowMode legit = new LegitNoSlowMode(this, Features.NO_SLOW_LEGIT);
        NcpNoSlowMode ncp = new NcpNoSlowMode(this, Features.NO_SLOW_NCP);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, custom, hypixel, rewound, swap, legit, ncp);
        clickWhileUsing.hideIf = () -> !mode.getValue().removesSlowdown();
        mode.addListener((oldValue, newValue) -> {
            NoSlowMode oldMode = (NoSlowMode) oldValue;
            NoSlowMode newMode = (NoSlowMode) newValue;
            if (isEnabled() && oldMode != newMode) {
                if (oldMode != null) oldMode.onDisable();
                Objects.requireNonNull(newMode).onEnable();
            }
        });
        getSettings().remove(mode);
        getSettings().add(0, mode);
    }

    public NoSlowItemType getItemType(boolean renderSlot) {
        if (mc.thePlayer == null) return null;
        ItemStack stack;
        if (renderSlot && Adjust.slotSpoofer.field17) {
            stack = Adjust.slotSpoofer.getRenderItemStack();
        } else {
            stack = mc.thePlayer.inventory.getCurrentItem();
        }
        if (stack == null) return null;
        Item item = Objects.requireNonNull(stack.getItem());
        if (item instanceof ItemBucketMilk) return NoSlowItemType.CONSUMABLE;
        if (item instanceof ItemSword) return NoSlowItemType.SWORD;
        if (item instanceof ItemFood) return NoSlowItemType.CONSUMABLE;
        if (item instanceof ItemBow) return NoSlowItemType.BOW;
        if (item instanceof ItemPotion && !ItemPotion.isSplash(stack.getMetadata())) return NoSlowItemType.CONSUMABLE;
        return null;
    }

    @EventHandler(priority = 63)
    public void onClick(IngameActionEvent event) {
        if (!isEnabled()) return;
        if (mc.thePlayer.isUsingItem() && clickWhileUsing.isEnabled()) {
            mc.processMouseClickHelper(Adjust.mouseClickHelper, event.actionStage, false);
        }
    }

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }

    public boolean isBlocking() {
        return isEnabled() && mode.getValue().isBlocking();
    }
}
