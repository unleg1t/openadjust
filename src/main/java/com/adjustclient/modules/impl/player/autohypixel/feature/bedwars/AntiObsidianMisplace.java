package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.IngameActionEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.modules.impl.player.autohypixel.feature.AutoHypixelFeature;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

public final class AntiObsidianMisplace extends AutoHypixelFeature {
    public static AntiObsidianMisplace INSTANCE;
    public final BooleanSetting enabled;

    public AntiObsidianMisplace(AutoHypixelModule owner, ExpandSetting parent) {
        super(owner);
        INSTANCE = this;
        enabled = new BooleanSetting(parent, "Anti Obsidian Misplace", true);
        enabled.setDescription("Prevents you from misplacing obsidian around a bed.");
    }

    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler(priority = 43) public void onTick(IngameActionEvent event) {
        if (event.suppressActions || !isActive() || mc.thePlayer == null) return;
        var state = Adjust.gameStateTracker.state;
        if (state.game != HypixelGame.BED_WARS || state.startTimer == null || state.ended) return;
        var use = Adjust.mouseClickHelper.right;
        // A queued click and a held key both count, but neither consumes the input.
        if (!use.isPressed(false) && !use.isClicked(false)) return;
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        if (held == null || held.getItem() != Item.getItemFromBlock(Blocks.obsidian)) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        BlockPos place = hit.getBlockPos().offset(hit.sideHit);
        for (EnumFacing side : EnumFacing.values()) {
            if (mc.theWorld.getBlockState(place.offset(side)).getBlock() == Blocks.bed) return;
        }
        use.disabled = true;
    }
}
