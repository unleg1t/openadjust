package com.adjustclient.module.impl.player.inventory;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.GuiOpenedEvent;
import com.adjustclient.event.ScreenKeyEvent;
import com.adjustclient.event.ScreenMouseClickEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.inventory.chest.ChestLootStrategy;
import com.adjustclient.module.impl.player.inventory.chest.DefaultChestStrategy;
import com.adjustclient.module.impl.player.inventory.chest.LuckyBlockChestStrategy;
import com.adjustclient.module.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import com.adjustclient.module.impl.player.inventory.item.ItemTypes;
import com.adjustclient.module.impl.player.inventory.plan.*;
import com.adjustclient.util.Cooldown;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.module.impl.player.inventory.item.ItemFilterMode;
import com.adjustclient.game.player.InventoryStateTracker;

public final class InventoryModule extends Module implements MinecraftInstance {
    public static InventoryModule INSTANCE;
    public InventorySettings inventorySettings;
    public HotbarLayout hotbarLayout;
    public InventorySnapshot snapshot;
    public ChestTracker chestTracker;
    public Cooldown closingCooldown;
    public Cooldown pauseCooldown;
    public Timer inactiveTimer;
    public ChestLootStrategy chestStrategy;
    public ItemFilterMode transferDirection;
    public boolean disablePending;
    public boolean field69;
    public boolean field70;
    public boolean field71;
    public boolean field72;
    public boolean field73;
    public boolean field74;

    public InventoryModule() {
        super("Inventory", Category.PLAYER);
        INSTANCE = this;
        inventorySettings = new InventorySettings(this);
        hotbarLayout = new HotbarLayout(this);
        snapshot = null;
        chestTracker = null;
        closingCooldown = null;
        pauseCooldown = new Cooldown(-1L);
        inactiveTimer = new Timer();
        chestStrategy = null;
        transferDirection = null;
        disablePending = false;
        field69 = false;
        field70 = false;
        field71 = false;
        field72 = false;
        field73 = false;
        field74 = false;
        com.adjustclient.module.impl.player.inventory.quickbuy.QuickBuyListener.INSTANCE =
                new com.adjustclient.module.impl.player.inventory.quickbuy.QuickBuyListener(
                        this);
    }

    public boolean B() {
        if (!K()) return false;
        for (InventorySlot slot : snapshot.slots)
            if (slot.slotId >= 0 && slot.slotId < 5 && slot.itemStack != null) return true;
        return false;
    }

    public boolean D() {
        return snapshot != null && E() && (K() || J())
                && !snapshot.unassignedSlots.isEmpty()
                && chestStrategy.canClose() && !B();
    }

    public boolean E() {
        if (!inventorySettings.managerEnabled.isEnabled()
                || inventorySettings.managerEnabled.isHidden()) {
            if (!(chestStrategy instanceof LuckyBlockChestStrategy)
                    || !inventorySettings.bedWarsAutoChest.isEnabled()
                    || inventorySettings.bedWarsAutoChest.isHidden()
                    || transferDirection == null || !(mc.currentScreen instanceof GuiChest)) return false;
        }
        if (pauseCooldown.durationMs >= 0 && !pauseCooldown.isExpired()) return false;
        var state = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        return state == null || state.antiCheatModel == null
                || state.antiCheatModel.allowsCapabilityC();
    }

    public void createSnapshot() {
        InventorySnapshot created = new InventorySnapshot(hotbarLayout, inventorySettings);
        created.clicker = new InventoryClicker(created);
        created.keepChecker = new ItemKeepChecker(created);
        this.snapshot = created;
        field69 = false;
        updateSnapshot(created, false);
        created.clicker.lastPosition = new com.adjustclient.util.Vec2f(4.0F, created.unassignedSlots.size() / 9.0F + 2.0F);
    }

    public boolean J() {
        if (mc.thePlayer == null || !(mc.thePlayer.openContainer instanceof net.minecraft.inventory.ContainerChest chest)) return false;
        if (!inventorySettings.validateChest.isEnabled()) return true;
        var inventory = chest.getLowerChestInventory();
        return inventory instanceof net.minecraft.client.player.inventory.ContainerLocalMenu menu
                && menu.hasCustomName() && menu.forkField21;
    }

    public static boolean K() {
        if (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainerCreative)
            return net.minecraft.client.gui.inventory.GuiContainerCreative.selectedTabIndex == 11;
        return mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiInventory
                || InventoryStateTracker.inServerInventory && com.adjustclient.Adjust.inventoryStateTracker.open;
    }

    public boolean L() {
        return inventorySettings.managerAutoArmor.isEnabled()
                && !inventorySettings.managerAutoArmor.isHidden()
                && chestStrategy instanceof DefaultChestStrategy;
    }

    @EventHandler public void onMouseInput(ScreenKeyEvent event) {
        if (!isEnabled()) return;
        if (event.pressed && D() && event.keyCode != org.lwjgl.input.Keyboard.KEY_ESCAPE
                && event.keyCode != mc.gameSettings.keyBindInventory.getKeyCode()) event.cancelled = true;
    }

    @EventHandler public void onMouseClick(ScreenMouseClickEvent event) {
        if (!isEnabled() || event.cancelled) return;
        if (chestStrategy instanceof LuckyBlockChestStrategy
                && inventorySettings.bedWarsAutoChest.isEnabled()
                && !inventorySettings.bedWarsAutoChest.isHidden()) {
            float wheel = org.lwjgl.input.Mouse.getEventDWheel();
            if (wheel != 0.0F && transferDirection == null) {
                snapshot = null;
                field70 = false;
                if (disablePending) setEnabled(false, true);
                transferDirection = wheel > 0.0F
                        ? ItemFilterMode.UP : ItemFilterMode.DOWN;
            }
        }
        if (!D() || mc.thePlayer == null) return;
        if (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainerCreative creative) {
            for (net.minecraft.creativetab.CreativeTabs tab : net.minecraft.creativetab.CreativeTabs.creativeTabArray) {
                if (creative.func_147049_a(tab, (int) event.mouseX - creative.guiLeft,
                        (int) event.mouseY - creative.guiTop)) return;
            }
        }
        event.cancelled = true;
    }

    @EventHandler public void onScreenOpen(GuiOpenedEvent event) {
        if (!isEnabled()) return;
        if (!(chestStrategy instanceof LuckyBlockChestStrategy)
                || !inventorySettings.bedWarsAutoChest.isEnabled()
                || !(event.screen instanceof GuiChest)) return;
        if ((com.adjustclient.service.config.user.UserConfiguration.INSTANCE.timestamp & 1L) != 0) return;
        var game = com.adjustclient.Adjust.gameStateTracker.state;
        if (game.startTimer != null && game.lobby != null
                && !game.lobby.booleanValue()) {
            var notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = "Inventory";
            notification.parts = new ArrayList<>();
            notification.format("Use your scroll wheel to move resources in or out of chests.");
            notification.lifetimeSteps = Integer.valueOf(40);
            notification.key = "bwautochest";
            notification.show();
        }
    }

    @EventHandler public void onTick(com.adjustclient.event.UserInputVerificationEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        z();
        if (disablePending && (snapshot == null
                || snapshot.clicker.clicks.isEmpty())) {
            toggle(false);
            return;
        }
        if (!E()) {
            snapshot = null;
            field70 = false;
            if (disablePending) toggle(false);
            field73 = false;
            field72 = false;
            return;
        }
        if (inventorySettings.managerEnabled.isEnabled()
                && !inventorySettings.managerEnabled.isHidden()
                && !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory && L()) {
            chestTracker.prepareArmorUse(() -> {
                if (snapshot == null) createSnapshot();
                return snapshot;
            });
        }
        if (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer gui
                && (gui.dragSplitting || gui.draggedStack != null)) {
            snapshot = null;
            field70 = false;
            if (disablePending) toggle(false);
            field73 = false;
            field72 = false;
            return;
        }
        if (!K() && !J()) {
            if (inactiveTimer.hasElapsed(1000L)) {
                inactiveTimer.reset();
                field73 = false;
            }
            snapshot = null;
            field70 = false;
            if (disablePending) toggle(false);
            field72 = false;
            return;
        }
        if (snapshot != null && snapshot.clicker.clicks.isEmpty()
                && field71) {
            field71 = false;
            snapshot = null;
            field70 = false;
            if (disablePending) toggle(false);
        }
        if (snapshot == null) createSnapshot();
        if (B()) { field74 = true; return; }
        boolean canClick = J() || K();
        InventoryClicker clicker = snapshot.clicker;
        if (!clicker.clicks.isEmpty() && canClick) { clicker.tick(); return; }
        if (chestStrategy.canClose()) {
            List<InventorySlot> excluded = new ArrayList<>(1);
            while (true) {
                InventorySlot next = chooseSlot(clicker.lastPosition, excluded);
                if (next == null) {
                    if (clicker.clicks.isEmpty()) transferDirection = null;
                    if (inventorySettings.managerEnabled.isEnabled()
                            && (field72 || inventorySettings.closeEmptyChests.isEnabled() && J())
                            && inventorySettings.automaticCloseEnabled.isEnabled()) {
                        if (closingCooldown != null) {
                            if (closingCooldown.isExpired()
                                    && mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer screen
                                    && (inventorySettings.closeInventory.isEnabled()
                                        || !(screen instanceof net.minecraft.client.gui.inventory.GuiInventory))
                                    && !field74) screen.closeContainer();
                        } else {
                            int delay = snapshot.settings.resolveClosingDelay();
                            int bound = snapshot.settings.resolveClosingDelay() / 5;
                            closingCooldown = new Cooldown(delay + com.adjustclient.util.RandomUtil.nextInt(bound));
                        }
                    }
                    break;
                }
                closingCooldown = null;
                excluded.add(next);
                if (processSlot(next)) { field72 = true; break; }
            }
        }
        if (!clicker.clicks.isEmpty() && canClick) clicker.tick();
    }

    public int priority(ItemStack stack) {
        var item = stack.getItem();
        var checker = snapshot.keepChecker;
        if (item instanceof net.minecraft.item.ItemArmor armor && checker.isUseful(stack))
            return mc.thePlayer.inventory.armorInventory[3 - armor.armorType] == null ? 3 : 4;
        if (item instanceof net.minecraft.item.ItemSword && checker.isUseful(stack)) return 2;
        if (item instanceof net.minecraft.item.ItemTool && checker.isUseful(stack)) return 1;
        if (item instanceof net.minecraft.item.ItemPotion) {
            for (ItemType type : ItemTypes.ALL) {
                if (type.matches(stack)) return type.keepFilter.isItemGood(type, stack,
                        snapshot) ? 0 : Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

    public InventorySlot chooseSlot(com.adjustclient.util.Vec2f position, List<InventorySlot> excluded) {
        InventorySlot best = null;
        int priority = Integer.MIN_VALUE;
        for (InventorySlot slot : snapshot.unassignedSlots) {
            if (excluded.contains(slot)) continue;
            int current = priority(slot.itemStack);
            if (current > priority) { priority = current; best = slot; }
        }
        if (best != null) return best;
        float nearest = Float.MAX_VALUE;
        for (InventorySlot slot : snapshot.unassignedSlots) {
            if (excluded.contains(slot)) continue;
            float dx = slot.position.x - position.x;
            float dy = slot.position.y - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (nearest > distance) { nearest = distance; best = slot; }
        }
        return best;
    }

    public boolean processSlot(InventorySlot slot) {
        if (field69) { field69 = false; updateSnapshot(snapshot, false); }
        var action = chestStrategy instanceof DefaultChestStrategy
                ? slot.inventory.chooseAction(slot) : chestStrategy.chooseAction(slot);
        if (action != null) action.execute(snapshot.clicker);
        snapshot.assignments.add(new SlotAssignment(slot.slotId, slot.inventory.getClass(),
                slot.itemStack.getItem()));
        field69 = false;
        updateSnapshot(snapshot, false);
        return action != null;
    }

    @Override public void onEnable() {
        if (chestTracker == null) chestTracker = new ChestTracker();
        java.util.Arrays.fill(chestTracker.armorTimers, null);
        disablePending = false;
        field69 = false;
        field71 = false;
        z();
    }

    @Override public void onDisable() {
        snapshot = null;
        field70 = false;
        if (disablePending) toggle(false);
        java.util.Arrays.fill(chestTracker.armorTimers, null);
        field73 = false;
        field74 = false;
    }

    public void z() {
        var state = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        boolean bedWars = state != null && state.antiCheatModel != null
                && state.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                && com.adjustclient.Adjust.gameStateTracker.state.game == com.adjustclient.hypixel.HypixelGame.BED_WARS;
        if (bedWars) {
            if (!(chestStrategy instanceof LuckyBlockChestStrategy))
                chestStrategy = new LuckyBlockChestStrategy(this);
        } else if (!(chestStrategy instanceof DefaultChestStrategy)) {
            chestStrategy = new DefaultChestStrategy(this);
        }
    }

    public void invalidateSnapshot() {
        if (!snapshot.clicker.clicks.isEmpty()) {
            field71 = true;
            return;
        }
        snapshot = null;
        field70 = false;
        if (disablePending) toggle(false);
    }

    @EventHandler public void onPacketReceive(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof net.minecraft.network.play.server.S30PacketWindowItems packet) {
            if (snapshot == null) return;
            if (!field70 && packet.func_148911_c() == mc.thePlayer.openContainer.windowId) {
                snapshot.clicker.resetOpeningDelay();
                field70 = true;
            }
            invalidateSnapshot();
        } else if (event.packet instanceof net.minecraft.network.play.server.S2FPacketSetSlot
                && snapshot != null) invalidateSnapshot();
    }

    public void fillPlayerSlots(InventorySnapshot snapshot) {
        ArmorPlanner planner = new ArmorPlanner(snapshot);
        for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
            if (slot == null || slot.getStack() == null) continue;
            ItemStack stack = slot.getStack();
            int slotId = slot.slotNumber;
            com.adjustclient.util.Vec2f position = slotId == -999
                    ? new com.adjustclient.util.Vec2f(0.0F, 0.0F)
                    : new com.adjustclient.util.Vec2f(slotId % 9, slotId / 9.0F);
            snapshot.addSlot(new InventorySlot(planner, stack, position, 0, slotId));
        }
    }

    public void updateSnapshot(InventorySnapshot snapshot, boolean forcePlayer) {
        snapshot.unassignedSlots.clear();
        snapshot.slots.clear();
        // Assignments persist across refreshes; they suppress slots already considered.
        boolean chest = J();
        boolean inventory = K();
        if (forcePlayer && !inventory && !chest) inventory = true;
        if (chest) fillChestSlots(snapshot, (net.minecraft.inventory.ContainerChest) mc.thePlayer.openContainer);
        else if (inventory) fillPlayerSlots(snapshot);
        var state = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (inventorySettings.validateChest.isEnabled()
                && !inventorySettings.validateChest.isHidden()
                && state != null && state.antiCheatModel != null
                && state.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            List<InventorySlot> ignored = snapshot.unassignedSlots.stream().filter(slot -> {
                ItemStack stack = slot.itemStack;
                return stack.hasTagCompound() && stack.getTagCompound().hasKey("display", 10)
                        && stack.getTagCompound().getCompoundTag("display").hasKey("Name", 8)
                        && stack.getDisplayName().endsWith("\u00a77 (Right Click)");
            }).toList();
            ignored.forEach(slot -> {
                snapshot.assignments.add(new SlotAssignment(slot.slotId,
                        slot.inventory.getClass(), slot.itemStack.getItem()));
            });
            snapshot.unassignedSlots.removeAll(ignored);
        }
        snapshot.autoArmor = inventory && !chest;
    }

    public void fillChestSlots(InventorySnapshot snapshot, net.minecraft.inventory.ContainerChest chest) {
        List<Slot> contents = new ArrayList<>(chest.inventorySlots);
        ItemPlanner chestPlanner = new ItemPlanner(snapshot, chest);
        ArmorPlanner playerPlanner = new ArmorPlanner(snapshot);
        for (Slot slot : contents) {
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            InventoryPlanner planner = slot.inventory == mc.thePlayer.inventory ? playerPlanner : chestPlanner;
            int slotId = slot.slotNumber;
            com.adjustclient.util.Vec2f position = slotId == -999
                    ? new com.adjustclient.util.Vec2f(0.0F, 0.0F)
                    : new com.adjustclient.util.Vec2f(slotId % 9, slotId / 9.0F);
            snapshot.addSlot(new InventorySlot(planner, stack, position, chest.windowId, slotId));
        }
        // The player's occupied equipment slots must remain visible to the keep filter,
        // but cannot be selected as ordinary chest-click destinations.
        for (int index = 5; index <= 7; index++) {
            Slot slot = mc.thePlayer.inventoryContainer.inventorySlots.get(index);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            int slotId = slot.slotNumber;
            com.adjustclient.util.Vec2f position = slotId == -999
                    ? new com.adjustclient.util.Vec2f(0.0F, 0.0F)
                    : new com.adjustclient.util.Vec2f(slotId % 9, slotId / 9.0F);
            InventorySlot equipped = new InventorySlot(playerPlanner, stack, position, chest.windowId, -index - 1);
            equipped.protectedSlot = true;
            snapshot.addSlot(equipped);
        }
    }

    @Override public void setEnabled(boolean enabled, boolean notify) {
        if (!enabled && mc.thePlayer != null && isEnabled() && snapshot != null
                && !snapshot.clicker.clicks.isEmpty()) {
            disablePending = true;
            return;
        }
        if (isEnabled() == enabled) return;
        flags = (byte) (enabled ? flags | 2 : flags & ~2);
        try {
            if (enabled) onEnable(); else onDisable();
        } catch (Throwable error) {
            reportToggleError(error, enabled);
        }
        if (notify && notifyToggle) com.adjustclient.Adjust.INSTANCE.fileSystem.markDirty();
    }
}
