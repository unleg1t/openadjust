package com.adjustclient.ui.clickgui;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.module.impl.render.ClickGui;
import com.adjustclient.ui.clickgui.frame.ElementDecorator;
import com.adjustclient.ui.clickgui.frame.TabHeader;
import com.adjustclient.ui.dialog.AdjustButton;
import com.adjustclient.util.Vec2f;
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
