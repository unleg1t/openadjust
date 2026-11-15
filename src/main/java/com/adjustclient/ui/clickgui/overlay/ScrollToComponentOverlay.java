package com.adjustclient.ui.clickgui.overlay;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.Modifier;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.util.Vec2f;

public class ScrollToComponentOverlay extends Modifier {
    private ClickGuiComponent target;
    private Object unused8;

    public ScrollToComponentOverlay(ClickGuiComponent target) { this.target = target; }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object unused, Vec2f mouse, boolean firstPass) {
        ClickGuiComponent comp = this.target;
        if (comp.owner != null) {
            float height = comp.owner.height;
            float y = comp.getY();
            if (y >= height - comp.height) {
                var tab = comp.owner;
                float delta = height - comp.getY();
                tab.scrollBy(delta);
            }
        }
        ClickGuiScreen.instance().removeOverlay(this);
    }
}
