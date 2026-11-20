package com.adjustclient.modules.impl.player;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.UtilityModule;

public final class NoChatCloseModule extends UtilityModule {
    public static NoChatCloseModule INSTANCE;
    public NoChatCloseModule() {
        super("No Chat Close", Category.PLAYER);
        INSTANCE = this;
        setHidden(false);
    }

    @Override public String getDescription() {
        return "Prevents the server from forcefully closing your chat.";
    }
}
