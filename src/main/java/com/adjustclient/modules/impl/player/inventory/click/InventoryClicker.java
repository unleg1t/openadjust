package com.adjustclient.modules.impl.player.inventory.click;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.player.inventory.InventorySettings;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot;
import com.adjustclient.modules.impl.player.inventory.plan.ItemKeepChecker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InventoryClicker implements MinecraftInstance {
    public final InventorySnapshot snapshot;
    public final List<WindowClick> clicks = new ArrayList<>();
    public Vec2f lastPosition;
    public final Cooldown clickDelay;
    public final Cooldown openingDelay;

    public InventoryClicker(InventorySnapshot snapshot) {
        this.snapshot = snapshot;
        clickDelay = new Cooldown(10L);
        openingDelay = new Cooldown(10L);
        resetClickDelay();
        resetOpeningDelay();
    }

    public void tick() {
        if (clicks.isEmpty() || !openingDelay.isExpired()) return;
        WindowClick click = clicks.get(0);
        if (click.windowId != mc.thePlayer.inventoryContainer.windowId
                && click.windowId != mc.thePlayer.openContainer.windowId) {
            clicks.remove(0);
            return;
        }
        Vec2f position = click.slot == -999 ? new Vec2f(0.0F, 0.0F)
                : new Vec2f(click.slot % 9, (float) click.slot / 9.0F);
        int delay = snapshot.settings.resolveClickingDelay();
        float dx = position.x - lastPosition.x;
        float dy = position.y - lastPosition.y;
        double extra = ((double) delay / 9.5) * ((double) (float) Math.sqrt(dx * dx + dy * dy) * 2.6);
        if (click.slot == -999) extra /= 2.6;
        if (!clickDelay.isExpired((long) extra)) return;
        resetClickDelay();
        clicks.remove(0);
        lastPosition = position;
        mc.playerController.windowClick(click.windowId, click.slot, click.button, click.mode, mc.thePlayer);
        ServerTracker.INSTANCE.field56.highlight(click.slot);
    }

    public void resetClickDelay() {
        InventorySettings settings = snapshot.settings;
        boolean bedWars = settings.bedWarsAutoChest.isEnabled()
                && !settings.bedWarsAutoChest.isHidden()
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS;
        int delay = bedWars ? 0 : settings.clickingDelay.getValue().intValue();
        int bound = bedWars ? 0 : settings.clickingDelay.getValue().intValue() / 3;
        int randomized = delay + (bound == 0 ? 0 : RandomUtil.RANDOM.nextInt(bound));
        clickDelay.timer.reset();
        clickDelay.durationMs = randomized;
    }

    public void resetOpeningDelay() {
        InventorySettings settings = snapshot.settings;
        boolean bedWars = settings.bedWarsAutoChest.isEnabled()
                && !settings.bedWarsAutoChest.isHidden()
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS;
        int delay = bedWars ? 0 : settings.openingDelay.getValue().intValue();
        int bound = bedWars ? 0 : settings.openingDelay.getValue().intValue() / 5;
        int randomized = delay + (bound == 0 ? 0 : RandomUtil.RANDOM.nextInt(bound));
        openingDelay.timer.reset();
        openingDelay.durationMs = randomized;
    }
}
