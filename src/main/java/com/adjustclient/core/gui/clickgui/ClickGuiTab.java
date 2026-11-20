package com.adjustclient.core.gui.clickgui;

import com.adjustclient.core.rendering.animation.Animation;
import java.util.ArrayList;
import com.adjustclient.core.gui.clickgui.frame.TabHeader;
import com.adjustclient.core.gui.screen.AdjustComponent;
import com.adjustclient.core.gui.screen.AdjustScreen;
import com.adjustclient.utilities.Vec2f;

public class ClickGuiTab extends AdjustComponent {
    public String title;
    public ClickGuiScreen screen;
    protected ArrayList<ClickGuiComponent> entries;
    protected ArrayList<ClickGuiComponent> renderList;
    protected ArrayList<ClickGuiComponent> rowList;
    protected ArrayList<ClickGuiComponent> layoutList;
    protected Animation scrollAnimation;
    public Vec2f anchor;
    protected TabHeader state;
    public float scroll;
    protected float contentHeight;
    public int id;
    public boolean hovered;

    /** Hit test: uses the clamped anchor position when present, else (x, y). */
    public boolean contains(Vec2f mouse) {
        float px = mouse.x;
        float py = mouse.y;
        float ox, oy;
        if (anchor != null) {
            ox = anchor.x;
            oy = anchor.y;
        } else {
            ox = this.x;
            oy = this.y;
        }
        boolean r = false;
        if (px >= ox && py >= oy) {
            r = (ox + this.width) > px && (oy + this.height) > py;
        }
        return r;
    }

    public TabHeader getState() {
        return state;
    }

    public ArrayList<ClickGuiComponent> getEntries() {
        return entries;
    }

    /** Scrolls the tab: scroll = max(0, min(scroll - delta, contentHeight - height)). */
    public void scrollBy(float delta) {
        float a = scroll - delta;
        float b = contentHeight - this.height;
        float m = Math.min(a, b);
        this.scroll = Math.max(0.0F, m);
    }

    public float getX() {
        return anchor != null ? anchor.x : this.x;
    }

    public float getY() {
        return anchor != null ? anchor.y : this.y;
    }

    /** x clamped so that the tab stays inside the window: max(0, min(x, windowWidth - width)). */
    public float clampX() {
        if (AdjustScreen.screenResolution == null) return Math.max(0.0F, this.x);
        float limit = Math.max(0.0F, (float) AdjustScreen.screenResolution.getScaledWidth() - this.width);
        return Math.max(0.0F, Math.min(this.x, limit));
    }

    /** y clamped so that the tab stays inside the window: max(0, min(y, windowHeight - height)). */
    public float clampY() {
        if (AdjustScreen.screenResolution == null) return Math.max(0.0F, this.y);
        float limit = Math.max(0.0F, (float) AdjustScreen.screenResolution.getScaledHeight() - this.height);
        return Math.max(0.0F, Math.min(this.y, limit));
    }
}
