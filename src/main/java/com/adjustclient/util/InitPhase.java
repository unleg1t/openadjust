package com.adjustclient.util;

import com.adjustclient.Adjust;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Bootstrap;
import com.adjustclient.changelog.Changelog;

public enum InitPhase {
    BOOTSTRAP(
            () -> Bootstrap.register()),
    GAME_SETTINGS(
            () -> {
                Minecraft minecraft = Minecraft.getMinecraft();
                minecraft.gameSettings = new GameSettings(minecraft, minecraft.mcDataDir);
            }),
    ADJUST(
            () -> initializeAdjust());

    public final Runnable action;
    public static long initializationMillis;

    InitPhase(Runnable action) { this.action = action; }

    public static void initializeAdjust() {
        long start = System.currentTimeMillis();
        Adjust.INSTANCE = new Adjust();
        long elapsed = System.currentTimeMillis() - start;
        initializationMillis = elapsed;
        Logger.log(LogLevel.INFO, true, "Adjust ({}) initialized in {}ms with {} modules and {} commands.",
                Changelog.latest.title, initializationMillis,
                Adjust.INSTANCE.moduleManager.view.size(),
                Adjust.INSTANCE.commandManager.view.size());
    }

    @SuppressWarnings("unused")
    public static void vendorCheckInitTime(long elapsed) {
        if (elapsed >= 8501L) {
            throw new RuntimeException();
        }
    }

}
