package com.adjustclient.modules.impl.player.inventory.action;
import com.adjustclient.modules.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.utilities.MinecraftInstance;
public abstract class InventoryAction implements MinecraftInstance {
    public abstract void execute(InventoryClicker clicker);
}
