package com.adjustclient.module.impl.player.inventory;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.impl.player.inventory.quickbuy.QuickBuySettings;
import com.adjustclient.server.ServerType;
import com.adjustclient.setting.*;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.item.ItemStack;

public final class InventorySettings {
    public BooleanSetting managerEnabled;
    public BooleanSetting managerAutoArmor;
    public NumberSetting openingDelay;
    public NumberSetting clickingDelay;
    public NumberSetting closingDelay;
    public BooleanSetting validateChest;
    public BooleanSetting bedWarsAutoChest;
    public BooleanSetting automaticCloseEnabled;
    public BooleanSetting closeEmptyChests;
    public BooleanSetting closeInventory;
    public HotbarSetting layout;
    public QuickBuySettings quickBuy;
    public BooleanSetting autoArmor;

    public InventorySettings(SettingContainer parent) {
        ExpandSetting manager = new ExpandSetting(parent, "Manager");
        manager.enabledPredicate = () -> managerEnabled.isEnabled();
        managerEnabled = new BooleanSetting(manager, "Enabled", true, "Yes", "No");
        ExpandSetting automaticClose = new ExpandSetting(manager, "Automatic close");
        automaticClose.enabledPredicate = () -> automaticCloseEnabled.isEnabled();
        automaticCloseEnabled = new BooleanSetting(automaticClose, "Enabled", true, "Yes", "No");
        closeEmptyChests = new BooleanSetting(automaticClose, "Close empty chests", true);
        closeEmptyChests.setDisplayName("closeEmptyChests2");
        closeInventory = new BooleanSetting(automaticClose, "Close inventory", true);
        ExpandSetting hotbar = new ExpandSetting(manager, "Hotbar");
        hotbar.enabledPredicate = () -> managerAutoArmor.isEnabled();
        managerAutoArmor = new BooleanSetting(hotbar, "Auto armor", true);
        autoArmor = managerAutoArmor;
        managerAutoArmor.setDisplayName("autoArmor");
        ExpandSetting delay = new ExpandSetting(manager, "Delay");
        delay.enabledPredicate = () -> {
            var server = ServerTracker.INSTANCE.serverState;
            boolean bedWars = server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS;
            return bedWarsAutoChest.isEnabled() && bedWars
                    ? false : openingDelay.getInt() != 0 || resolveClickingDelay() != 0;
        };
        openingDelay = new NumberSetting(delay, "Opening", 140, 0, 500, 10).formatter(NumberSetting.MILLISECONDS);
        clickingDelay = new NumberSetting(delay, "Clicking", 40, 0, 200, 10).formatter(NumberSetting.MILLISECONDS);
        closingDelay = new NumberSetting(delay, "Closing", 60, 0, 400, 10).formatter(NumberSetting.MILLISECONDS);
        layout = new HotbarSetting(manager, "Layout", new ItemStack[9]);
        validateChest = new BooleanSetting(manager, "Validate chest", true);
        ExpandSetting addons = new ExpandSetting(parent, "Addons");
        addons.enabledPredicate = () -> bedWarsAutoChest.isEnabled();
        bedWarsAutoChest = new BooleanSetting(addons, "Bed Wars Auto Chest", true);
        quickBuy = new QuickBuySettings(addons);
    }

    public int resolveClosingDelay() {
        if (bedWarsAutoChest.isEnabled()) {
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS) return 0;
        }
        return closingDelay.getInt();
    }
    public int resolveClickingDelay() {
        if (bedWarsAutoChest.isEnabled()) {
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS) return 0;
        }
        return clickingDelay.getInt();
    }
}
