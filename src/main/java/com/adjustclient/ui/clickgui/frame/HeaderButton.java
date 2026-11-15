package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.ui.screen.AdjustComponent;

public abstract class HeaderButton extends AdjustComponent {

    public boolean drawImmediately;

    public boolean isShown() {
        return true;
    }

    public abstract boolean onClick();

    public abstract void render();
}
