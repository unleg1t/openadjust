package com.adjustclient.core.gui.clickgui;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.utilities.Vec2f;
public abstract class Modifier {

    public ModifierPhase getPhase() {
        return ModifierPhase.PRE;
    }

    public boolean onScroll(float direction) {
        return false;
    }

    public boolean canStack() {
        return true;
    }

    public boolean keyTyped(int typedChar, int keyCode) {
        return false;
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        return false;
    }

    public abstract void render(ScaledResolution window, Render2DEvent queue, Object drawList, Vec2f mouse, boolean firstPass);

    public void reset() {
    }
}
