package com.adjustclient.core.gui.clickgui.overlay;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.clickgui.Modifier;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.utilities.Vec2f;

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
