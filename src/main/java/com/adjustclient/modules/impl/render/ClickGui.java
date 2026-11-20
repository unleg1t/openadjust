package com.adjustclient.modules.impl.render;

import com.adjustclient.Adjust;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.config.GlobalConfigsScreen;

public final class ClickGui {
    public static final GlobalConfigsScreen globalConfigurations;
    private ClickGui() { }
    static { globalConfigurations = new GlobalConfigsScreen(); }
    public static ClickGuiScreen openClickGui() {
        ClickGuiScreen screen = ClickGuiScreen.instance();
        Adjust.INSTANCE.screenStack.addOverlay(screen);
        return screen;
    }
    public static boolean isOpen() {
        return Adjust.INSTANCE.screenStack.overlays.contains(ClickGuiScreen.instance());
    }
    public static void openGlobalConfigurations() {
        Adjust.INSTANCE.screenStack.addOverlay(globalConfigurations);
    }
}
