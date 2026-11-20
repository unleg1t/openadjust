package com.adjustclient.core.gui.clickgui;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.render.ClickGui;
import com.adjustclient.core.gui.clickgui.frame.ElementDecorator;
import com.adjustclient.core.gui.clickgui.frame.TabHeader;
import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.utilities.Vec2f;
public final class IconButtonModifier extends ElementDecorator {
    private final ClickGuiComponent anchor;
    private final AdjustButton button;
    private final Runnable action;
    private final float rightOffset;
    private Object unused20;

    public IconButtonModifier(ClickGuiComponent anchor, AdjustButton button, Runnable action, float rightOffset) {
        this.anchor = anchor;
        this.button = button;
        this.action = action;
        this.rightOffset = rightOffset;
    }

    @Override
    public ModifierPhase getPhase() {
        return ModifierPhase.POST;
    }

    @Override
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (!pressed || button != 0) {
            return false;
        }
        AdjustButton b = this.button;
        if (!b.isHovered(mouse.x, mouse.y)) {
            return false;
        }
        action.run();
        return true;
    }

    /** Positions the button in the top right corner of the anchor tab and draws it. */
    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object unused, Vec2f mouse, boolean firstPass) {
        ClickGuiComponent tab = this.anchor;
        AdjustButton b = this.button;
        b.x = ((tab.x + tab.width) - (float) b.width) - this.rightOffset - 2.0F;
        b.y = (tab.y + 2.0F) + 0.5F;
        b.draw(mouse.x, mouse.y); // AdjustButton.draw(button, mouseX, mouseY)
    }

    static int multiplyAlpha(int alpha) {
        return (int) ((float) alpha * 0.75F);
    }
}
