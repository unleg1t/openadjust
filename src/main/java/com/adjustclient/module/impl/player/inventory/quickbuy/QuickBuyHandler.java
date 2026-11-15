package com.adjustclient.module.impl.player.inventory.quickbuy;

import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Cooldown;
import java.util.ArrayDeque;
import java.util.EnumMap;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

public final class QuickBuyHandler implements MinecraftInstance {
    public final QuickBuySettings settings;
    public final ArrayDeque<QuickBuyItem> queue = new ArrayDeque<>();
    public final EnumMap<QuickBuyItem, QuickBuyRequest> requests = new EnumMap<>(QuickBuyItem.class);
    public final Timer pageTimer = new Timer();
    public final Timer missingTimer = new Timer();
    public final Cooldown cooldown = new Cooldown(0);
    public QuickBuyItem current;
    public QuickBuyItem lastQuick;
    public ShopCategory requestedPage;
    public ShopCategory returnPage;
    public ShopCategory lastPage;
    public QuickBuyItem missingItem;
    public ShopCategory missingPage;
    public boolean quick;

    public QuickBuyHandler(QuickBuySettings settings) { this.settings = settings; }

    public void enqueue(QuickBuyItem item) {
        if (current != item && !queue.contains(item)) queue.addLast(item);
    }

    public void update(ContainerChest container, ShopCategory page) {
        if (returnPage != null) {
            if (page == ShopCategory.QUICK_BUY) {
                returnPage = null; lastPage = null; requestedPage = null;
            } else if (returnPage == page && page.slot >= 0) {
                if (cooldown.isExpired()) changePage(container, ShopCategory.QUICK_BUY);
                return;
            } else {
                returnPage = null; lastPage = null; requestedPage = null;
            }
        }
        if (current == null) {
            current = queue.peekFirst();
            quick = false;
            if (current == null) { current = nextQuick(page); quick = current != null; }
        }
        if (current == null) {
            if (lastPage != null) {
                if (lastPage == page && page.slot >= 0) returnPage = page;
                lastPage = null;
            }
            return;
        }
        if (page == ShopCategory.QUICK_BUY ? current.category.slot >= 0 : current.category == page) {
            int end = page == ShopCategory.UPGRADES
                    ? Math.min(27, container.getLowerChestInventory().getSizeInventory())
                    : container.getLowerChestInventory().getSizeInventory() - 9;
            for (int index = page == ShopCategory.UPGRADES ? 9 : 18; index < end; index++) {
                if (index >= container.inventorySlots.size()) break;
                Slot slot = container.inventorySlots.get(index);
                if (slot != null && slot.getHasStack() && current.matcher.test(slot.getStack())) {
                    purchase(container, page, slot.slotNumber);
                    return;
                }
            }
        }
        if (waitForMissing(page)) return;
        if (page == ShopCategory.QUICK_BUY && current.category.slot >= 0) {
            changePage(container, current.category); return;
        }
        if (page != ShopCategory.QUICK_BUY && page.slot >= 0 && current.category.slot < 0) {
            changePage(container, ShopCategory.QUICK_BUY); return;
        }
        if (page.slot >= 0 && current.category != page && current.category.slot >= 0) {
            changePage(container, current.category); return;
        }
        if (!quick && page != ShopCategory.QUICK_BUY && page.slot >= 0) returnPage = page;
        finishCurrent();
    }

    private void purchase(ContainerChest container, ShopCategory page, int slot) {
        missingItem = null; missingPage = null; requestedPage = null;
        if (!cooldown.isExpired()) return;
        requests.put(current, new QuickBuyRequest(page));
        int quickslot = settings.getQuickslot(current);
        if (Integer.compareUnsigned(quickslot, 9) < 0) click(container, slot, quickslot, 2);
        else mc.playerController.windowClick(container.windowId, slot, 0, 3, mc.thePlayer);
        cooldown.timer.reset();
        cooldown.durationMs = current.category == ShopCategory.UPGRADES ? 300 : quick ? 90 : 200;
        if (page != ShopCategory.QUICK_BUY && page.slot >= 0) {
            if (quick) lastPage = page;
            else { returnPage = page; lastPage = null; }
        }
        finishCurrent();
    }

    private void click(ContainerChest container, int slot, int button, int mode) {
        mc.playerController.windowClick(container.windowId, slot, button, mode, mc.thePlayer);
    }

    public void enqueueKey(QuickBuyItem item) {
        var setting = settings.quickSettings.get(item);
        if (setting.isEnabled() && !setting.isHidden()) return;
        queue.addLast(item);
    }

    private QuickBuyItem nextQuick(ShopCategory page) {
        QuickBuyItem[] items = QuickBuyItem.values();
        int start = lastQuick == null ? -1 : lastQuick.ordinal();
        for (int count = 1; count <= items.length; count++) {
            QuickBuyItem item = items[(start + count) % items.length];
            if (page == ShopCategory.QUICK_BUY ? item.category.slot < 0 : item.category != page) continue;
            var setting = settings.quickSettings.get(item);
            if (!setting.isEnabled() || setting.isHidden()) continue;
            if (settings.keybinds.get(item).isDown()) return item;
        }
        return null;
    }

    private void finishCurrent() {
        if (quick) lastQuick = current;
        else queue.pollFirst();
        current = null; quick = false; requestedPage = null; missingItem = null; missingPage = null;
    }

    private boolean waitForMissing(ShopCategory page) {
        QuickBuyRequest request = requests.get(current);
        if (request != null && request.category == page) {
            if (!request.timer.hasElapsed(500)) return true;
            requests.remove(current);
        }
        if (page == ShopCategory.QUICK_BUY ? current.category.slot < 0 : current.category != page) return false;
        if (missingItem == current && missingPage == page) {
            if (!missingTimer.hasElapsed(100)) return true;
            missingItem = null; missingPage = null;
            return false;
        }
        missingItem = current; missingPage = page; missingTimer.reset();
        return true;
    }

    private void changePage(ContainerChest container, ShopCategory page) {
        if (page.slot < 0) { finishCurrent(); return; }
        if (requestedPage == page && !pageTimer.hasElapsed(250)) return;
        click(container, page.slot, 0, 3);
        requestedPage = page;
        pageTimer.reset();
    }

    public void reset() {
        queue.clear(); requests.clear(); current = null; lastQuick = null; quick = false;
        requestedPage = null; returnPage = null; lastPage = null; missingItem = null; missingPage = null;
        cooldown.timer.reset(); cooldown.durationMs = 0;
    }
}
