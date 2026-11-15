package com.adjustclient.service.misc;

import com.adjustclient.Adjust;
import com.adjustclient.module.Module;
import com.adjustclient.module.UtilityModule;
import com.adjustclient.service.BackendMessageHandler;
import java.util.ArrayList;
import java.util.List;

public final class CollectionSyncHandler implements BackendMessageHandler {
    public final List<UtilityModule> modules = new ArrayList<>();
    public static boolean initialized;

    public void onDisconnect() {
    }

    public void onConnect() {
        if (!initialized) {
            initialized = true;
            for (Module module : Adjust.INSTANCE.moduleManager.view) {
                if (module instanceof UtilityModule) {
                    modules.add((UtilityModule) module);
                }
            }
        }
    }
}
