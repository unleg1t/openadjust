package com.adjustclient.core.gui.clickgui.setting.color;

import com.adjustclient.utilities.Vec2f;
import com.adjustclient.core.gui.screen.AdjustComponent;

public abstract class ColorPickerElement extends com.adjustclient.core.gui.screen.AdjustComponent {

    protected ColorPicker picker;

    public abstract void render();

    public abstract void updateFromColor();

    public abstract void setFromMouse(Vec2f mouse);
}
