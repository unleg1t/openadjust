package com.adjustclient.module.impl.combat.autoheal;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ItemsSetting;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import net.minecraft.item.*;
import net.minecraft.network.play.server.*;

public final class AutoHealModule extends Module implements MinecraftInstance {
    public static AutoHealModule INSTANCE;
    public static final HealHandler[] HEAL_HANDLERS = {new HypixelHeadHealHandler(), new RegenHealHandler(), new DefaultHealHandler()};
    public final ItemsSetting healingItems;
    public final HealItemSettings[] itemSettings;
    public AutoHealModule() {
        super("Auto Heal", Category.COMBAT);
        INSTANCE = this;
        int[] available = {397, 282, 393, 378, 383, 423};
        healingItems = new ItemsSetting(this, "Healing Items", new IntArrayList(new int[] {397, 282}), new IntArrayList(available), null);
        itemSettings = new HealItemSettings[available.length];
        for (int i = 0; i < available.length; i++) {
            int id = available[i];
            itemSettings[i] = new HealItemSettings(this, Item.getItemById(id),
                    () -> !healingItems.contains(id));
        }
    }
    @EventHandler(priority = 47) public void onTick(IngameActionEvent event) {
        if (event.suppressActions || !isEnabled() || mc.thePlayer == null) return;
        ServerTracker tracker = ServerTracker.INSTANCE;
        if (tracker.usedItemSinceMovement || tracker.attackedEntity != null
                || Adjust.mouseClickHelper.right.disabled || mc.thePlayer.isUsingItem()) return;
        var game = Adjust.gameStateTracker.state.game;
        if (game.isAnyOf(com.adjustclient.hypixel.HypixelGame.BED_WARS, com.adjustclient.hypixel.HypixelGame.SKYWARS,
                com.adjustclient.hypixel.HypixelGame.SHEEP_WARS, com.adjustclient.hypixel.HypixelGame.SKYBLOCK,
                com.adjustclient.hypixel.HypixelGame.HOUSING)) return;
        var blocker = tracker.serverState;
        int ping = blocker == null || blocker.packetWatcher.delayCount == 0 ? 0
                : blocker.packetWatcher.totalDelay / blocker.packetWatcher.delayCount;
        boolean urgent = false;
        if (Math.min(10, (int) Math.ceil(ping / 50.0D * 1.3D)) > tracker.ticksSinceAttack
                && tracker.attackedLivingEntity != null) {
            var target = tracker.attackedLivingEntity;
            TrackedLivingEntity tracked = tracker.entityTracker.get(target.getEntityId(), TrackedLivingEntity.class);
            if (tracked != null && !target.isDead && mc.thePlayer.getDistanceSqToEntity(target) < 36.0D) {
                float health = target.getHealth() + target.getAbsorptionAmount();
                if (health <= 0.0F) return;
                float damage = DamageUtil.computeDamage(target, mc.thePlayer, mc.thePlayer.inventory.getCurrentItem(), false).damage;
                if (System.currentTimeMillis() - tracked.combatStats.lastAttack <= ping * 1.25D && health - damage <= 0.0F) return;
                urgent = damage * 1.75F > health;
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null) continue;
            for (HealItemSettings settings : itemSettings) {
                if (!healingItems.contains(Item.getIdFromItem(settings.item)) || settings.item != stack.getItem()) continue;
                if (!settings.tryHeal(stack, urgent)) break;
                useItem(slot, settings);
                return;
            }
        }
    }
    public void useItem(int slot, HealItemSettings settings) {
        var switcher = Adjust.slotSpoofer;
        switcher.slot = slot;
        switcher.active = true;
        switcher.field8 = switcher.field12;
        switcher.field17 = false;
        switcher.renderSpoofed = false;
        var click = Adjust.mouseClickHelper.right;
        click.clicks = 1;
        click.down = Boolean.TRUE;
        click.holdTicks = 0;
        Adjust.mouseClickHelper.left.disabled = true;
        if (settings.cooldown == null) settings.cooldown = new Timer();
        else settings.cooldown.reset();
        settings.useTimer.reset();
    }
    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof S30PacketWindowItems) {
            Arrays.stream(itemSettings).forEach(settings -> settings.cooldown = null);
        } else if (event.packet instanceof S2FPacketSetSlot && mc.thePlayer != null) {
            S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;
            int slot = packet.func_149173_d();
            if (packet.func_149175_c() != mc.thePlayer.inventoryContainer.windowId || slot < 0 || slot >= 9) return;
            ItemStack oldStack = mc.thePlayer.inventory.mainInventory[slot];
            ItemStack newStack = packet.func_149174_e();
            if (oldStack != null && oldStack != newStack) clearCooldown(oldStack.getItem());
            else if (oldStack == null && newStack != null) clearCooldown(newStack.getItem());
        }
    }
    public void clearCooldown(Item item) {
        Arrays.stream(itemSettings).forEach(settings -> {
            if (settings.item == item) settings.cooldown = null;
        });
    }
}
