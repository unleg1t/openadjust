package com.adjustclient.module.impl.player;

import com.adjustclient.module.Category;
import com.adjustclient.module.UtilityModule;

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
