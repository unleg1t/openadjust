package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.gui.screen.AdjustComponent;

public abstract class HeaderButton extends AdjustComponent {

    public boolean drawImmediately;

    public boolean isShown() {
        return true;
    }

    public abstract boolean onClick();

    public abstract void render();
}
