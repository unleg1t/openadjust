package com.adjustclient.ui.clickgui.setting.items;

import com.adjustclient.util.Vec2f;

public abstract class ItemListEntry extends com.adjustclient.ui.screen.AdjustComponent {

    public abstract void draw(Vec2f mouse, com.adjustclient.event.Render2DEvent queue, boolean hovered);
}
