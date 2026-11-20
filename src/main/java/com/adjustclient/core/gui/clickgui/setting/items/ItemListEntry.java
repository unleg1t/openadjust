package com.adjustclient.core.gui.clickgui.setting.items;

import com.adjustclient.utilities.Vec2f;

public abstract class ItemListEntry extends com.adjustclient.core.gui.screen.AdjustComponent {

    public abstract void draw(Vec2f mouse, com.adjustclient.core.events.Render2DEvent queue, boolean hovered);
}
