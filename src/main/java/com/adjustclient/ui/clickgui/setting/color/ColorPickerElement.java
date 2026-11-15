package com.adjustclient.ui.clickgui.setting.color;

import com.adjustclient.util.Vec2f;
import com.adjustclient.ui.screen.AdjustComponent;

public abstract class ColorPickerElement extends com.adjustclient.ui.screen.AdjustComponent {

    protected ColorPicker picker;

    public abstract void render();

    public abstract void updateFromColor();

    public abstract void setFromMouse(Vec2f mouse);
}
