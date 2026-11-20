package com.adjustclient.modules.impl.player.autorewound.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.events.AttackEntityEvent;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.ScoreboardRenderEvent;
import com.adjustclient.utilities.game.rewound.RewoundGame;
import com.adjustclient.utilities.game.rewound.RewoundPitGameState;
import com.adjustclient.modules.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.modules.impl.player.autorewound.PitChatPatterns;
import com.adjustclient.modules.impl.player.autorewound.PitSwordTier;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.KeybindSetting;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import com.adjustclient.utilities.anticheat.entity.TrackedFakePlayer;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.StringUtils;

public final class PitFeature extends AutoRewoundFeature {
    public static PitFeature INSTANCE;
    public final BooleanSetting enabled;
    public final BooleanSetting showActiveStreak;
    public final BooleanSetting spadeSwap;
    public final Bind spadeSwapHoldBind;
    public final BooleanSetting swordSwap;
    public final Bind swordSwapHoldBind;
    public final BooleanSetting hideAssists;
    public final BooleanSetting hideKills;
    public final BooleanSetting hideStreaks;
    public final BooleanSetting hideBounties;
    public final BooleanSetting hideBountiesClaimed;
    public final BooleanSetting hideNightQuest;
    public final BooleanSetting hideMegaStreak;
    public final BooleanSetting hideApostle;
    private boolean spoofingSlot;

    public PitFeature(AutoRewoundModule owner) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(owner, "Pit");
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = () -> enabled.isEnabled();

        showActiveStreak = new BooleanSetting(group, "Show active streak", false, "On", "Off");
        showActiveStreak.setDescription("Shows your current streak stats under the scoreboard");

        ExpandSetting spadeGroup = new ExpandSetting(group, "Spade Swap");
        spadeGroup.setDescription("Spade swaps automatically when attacking someone in diamond armor");
        spadeSwap = new BooleanSetting(spadeGroup, "Enabled", true, "Yes", "No");
        spadeGroup.enabledPredicate = () -> spadeSwap.isEnabled();
        spadeSwapHoldBind = Adjust.INSTANCE.bindManager.register("module.autorewound.pit.spadeswap.holdbind", mode -> {
        }, "Spade Swap Hold Bind");
        new KeybindSetting(spadeGroup, "Hold Bind", spadeSwapHoldBind)
                .setDescription("Always spade swaps if not bound");

        ExpandSetting swordGroup = new ExpandSetting(group, "Sword Swap");
        swordGroup.setDescription("Swaps to a different sword when attacking someone for more damage");
        swordSwap = new BooleanSetting(swordGroup, "Enabled", true, "Yes", "No");
        swordGroup.enabledPredicate = () -> swordSwap.isEnabled();
        swordSwapHoldBind = Adjust.INSTANCE.bindManager.register("module.autorewound.pit.swordswap.holdbind", mode -> {
        }, "Sword Swap Hold Bind");
        new KeybindSetting(swordGroup, "Hold Bind", swordSwapHoldBind)
                .setDescription("Always sword swaps if not bound");

        ExpandSetting chatGroup = new ExpandSetting(group, "Chat");
        hideAssists = new BooleanSetting(chatGroup, "Hide assists", false, "On", "Off");
        hideAssists.setDescription("Hides assist messages from chat");
        hideKills = new BooleanSetting(chatGroup, "Hide kills", false, "On", "Off");
        hideKills.setDescription("Hides kill messages from chat");
        hideStreaks = new BooleanSetting(chatGroup, "Hide streaks", false, "On", "Off");
        hideStreaks.setDescription("Hides streak messages from chat");
        hideBounties = new BooleanSetting(chatGroup, "Hide bounties", false, "On", "Off");
        hideBounties.setDescription("Hides bounty messages from chat");
        hideBountiesClaimed = new BooleanSetting(chatGroup, "Hide bounties claimed", false, "On", "Off");
        hideBountiesClaimed.setDescription("Hides bounty claimed messages from chat");
        hideNightQuest = new BooleanSetting(chatGroup, "Hide night quest", false, "On", "Off");
        hideNightQuest.setDescription("Hides night quest messages from chat");
        hideMegaStreak = new BooleanSetting(chatGroup, "Hide mega streak", false, "On", "Off");
        hideMegaStreak.setDescription("Hides mega streak messages from chat");
        hideApostle = new BooleanSetting(chatGroup, "Hide apostle", false, "On", "Off");
        hideApostle.setDescription("Hides apostle messages from chat");
    }

    @Override
    protected boolean isFeatureEnabled() {
        return enabled.isEnabled()
                && !enabled.isHidden()
                && Adjust.rewoundGameTracker.state.game == RewoundGame.PIT;
    }

    public boolean shouldShowActiveStreak() {
        return isActive() && showActiveStreak.isEnabled() && !showActiveStreak.isHidden();
    }

    public static boolean hasDiamondArmor(EntityPlayer player) {
        for (ItemStack armorStack : player.inventory.armorInventory) {
            if (armorStack != null
                    && armorStack.getItem() instanceof ItemArmor armor
                    && armor.getArmorMaterial() == ItemArmor.ArmorMaterial.DIAMOND) {
                return true;
            }
        }
        return false;
    }

    public static Integer findSpadeSlot() {
        if (mc.thePlayer == null) return null;
        ItemStack[] hotbar = mc.thePlayer.inventory.mainInventory;
        for (int slot = 0; slot < 9; slot++) {
            if (hotbar[slot] != null && hotbar[slot].getItem() == Items.diamond_shovel) {
                return slot;
            }
        }
        return null;
    }

    public Integer findAlternativeSwordSlot(int excludedSlot) {
        int currentSlot = (!Adjust.slotSpoofer.active || Adjust.slotSpoofer.renderSpoofed)
                ? mc.thePlayer.inventory.m()
                : mc.thePlayer.inventory.currentItem;
        int bestRank = -1;
        Integer bestSlot = null;
        for (int i = 0; i < 9; i++) {
            int slot = (currentSlot + i) % 9;
            if (slot == excludedSlot) continue;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            if (stack == null || !(stack.getItem() instanceof ItemSword)) continue;
            PitSwordTier tier = PitSwordTier.fromStack(stack);
            if (tier != null && tier.rank > bestRank) {
                bestRank = tier.rank;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    public Integer findSwapSlot(EntityPlayer target) {
        int currentSlot = (!Adjust.slotSpoofer.active || Adjust.slotSpoofer.renderSpoofed)
                ? mc.thePlayer.inventory.m()
                : mc.thePlayer.inventory.currentItem;
        boolean canSwordSwap = swordSwap.isEnabled() && !swordSwap.isHidden() && swordSwapHoldBind.isHeld();
        if (spadeSwap.isEnabled() && !spadeSwap.isHidden() && spadeSwapHoldBind.isHeld() && hasDiamondArmor(target)) {
            Integer spadeSlot = findSpadeSlot();
            if (spadeSlot != null) {
                if (currentSlot == spadeSlot && canSwordSwap) {
                    Integer alt = findAlternativeSwordSlot(spadeSlot);
                    if (alt != null) {
                        return alt;
                    }
                }
                return spadeSlot;
            }
        }
        if (!canSwordSwap) return null;
        ItemStack currentStack = mc.thePlayer.inventory.mainInventory[currentSlot];
        if (PitSwordTier.fromStack(currentStack) == null) return null;
        return findAlternativeSwordSlot(currentSlot);
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!event.pre) {
            if (spoofingSlot) {
                spoofingSlot = false;
                Adjust.slotSpoofer.field17 = false;
                Adjust.slotSpoofer.active = false;
                mc.playerController.syncCurrentPlayItem();
            }
            return;
        }
        spoofingSlot = false;
        if (!isActive() || Adjust.slotSpoofer.active) return;
        if (!(event.entity instanceof EntityPlayer target)) return;
        if (ServerTracker.INSTANCE.entityTracker != null) {
            TrackedEntity<?> tracked = ServerTracker.INSTANCE.entityTracker.get(target.getEntityId(), TrackedEntity.class);
            if (!(tracked instanceof TrackedFakePlayer fakePlayer) || fakePlayer.removed || fakePlayer.stale) {
                return;
            }
        }
        if (mc.playerController.currentPlayerItem != mc.thePlayer.inventory.currentItem) return;
        ItemStack heldStack = Adjust.slotSpoofer.getRenderItemStack();
        if (heldStack == null || !(heldStack.getItem() instanceof ItemSword)) return;
        Integer swapSlot = findSwapSlot(target);
        if (swapSlot != null) {
            Adjust.slotSpoofer.slot = swapSlot;
            Adjust.slotSpoofer.active = true;
            Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
            Adjust.slotSpoofer.field17 = true;
            Adjust.slotSpoofer.renderSpoofed = false;
            spoofingSlot = true;
            mc.playerController.syncCurrentPlayItem();
        }
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (!isActive() || event.outgoing) return;
        if (!(event.packet instanceof S02PacketChat chat) || chat.getType() == 2) return;
        String text = StringUtils.stripControlCodes(chat.getChatComponent().getUnformattedText());
        if (matches(hideAssists, PitChatPatterns.ASSIST, text)
                || matches(hideKills, PitChatPatterns.KILL, text)
                || matches(hideStreaks, PitChatPatterns.STREAK, text)
                || matches(hideBounties, PitChatPatterns.BOUNTY, text)
                || matches(hideBountiesClaimed, PitChatPatterns.BOUNTY_CLAIMED, text)
                || matches(hideNightQuest, PitChatPatterns.NIGHT_QUEST, text)
                || matches(hideMegaStreak, PitChatPatterns.MEGASTREAK, text)
                || matches(hideApostle, PitChatPatterns.RNGESUS, text)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onScoreboardRender(ScoreboardRenderEvent event) {
        if (Adjust.rewoundGameTracker != null
                && Adjust.rewoundGameTracker.state instanceof RewoundPitGameState pitState) {
            pitState.onScoreboardRender(event);
        }
    }

    private static boolean matches(BooleanSetting setting, Pattern pattern, String message) {
        return setting.isEnabled() && !setting.isHidden() && pattern.matcher(message).find();
    }
}
