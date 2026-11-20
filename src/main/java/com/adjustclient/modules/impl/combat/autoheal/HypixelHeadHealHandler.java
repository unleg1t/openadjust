package com.adjustclient.modules.impl.combat.autoheal;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.game.pit.PitGameState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatColor;

final class HypixelHeadHealHandler extends RegenHealHandler {
    @Override public boolean accepts(Item item) {
        return item == Items.skull && Adjust.gameStateTracker.state.game == HypixelGame.THE_HYPIXEL_PIT;
    }
    @Override public boolean shouldHeal(HealItemSettings settings, ItemStack stack, boolean urgent) {
        if (!(Adjust.gameStateTracker.state instanceof PitGameState)) return super.shouldHeal(settings, stack, urgent);
        PitGameState state = (PitGameState) Adjust.gameStateTracker.state;
        if (state.nearSpawn || !state.locationTimer.hasElapsed(1500L) || !settings.useTimer.hasElapsed(500L)) return false;
        if (stack.getDisplayName().startsWith(ChatColor.RED.toString())) return false;
        if (stack.stackSize == 2 && !hasStrongRegeneration() && urgent && settings.useTimer.hasElapsed(1500L)) return true;
        return super.shouldHeal(settings, stack, urgent);
    }
}
