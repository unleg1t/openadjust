package com.adjustclient.utilities;

import java.util.ArrayList;
import java.util.List;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import net.minecraft.client.Minecraft;

public final class ClientBootstrap {
    public static final List<InitPhase> completedPhases = new ArrayList<>();

    public static void runPhase(InitPhase phase) {
        try {
            phase.action.run();
            completedPhases.add(phase);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Logger.log(LogLevel.ERROR, true, "Error inside initialization phase '{}': {}", phase.name(), throwable);
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft != null) {
                minecraft.mcThread.interrupt();
            }
            System.exit(phase.ordinal() + 1000);
        }
    }
}
