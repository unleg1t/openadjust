package com.adjustclient.ui.hud;

import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.game.movable.MovablePosition;
import com.adjustclient.render.NvgRenderer;
import com.adjustclient.ui.tabgui.TabGui;

public abstract class HudElement implements MinecraftInstance {

    protected Anchor movable;

    public boolean onKey(int key) {
        return false;
    }

    /**
     * Builds (re)fills and returns the draw list ({@code NvgRenderer}) of this element for the given screen.
     */
    public NvgRenderer build(ScaledResolution sr) {
        return null;
    }

    public boolean onMouse(int a2, int a3) {
        return false;
    }

    public void postRender(NvgRenderer list, ScaledResolution sr, Rect2f bounds) {
    }

    /**
     * Whether this element is currently active. Watermark, Information and Toggled return a constant true (their render()
     * checks the individual settings), PotionEffects returns true, the Tab GUI returns its "Enabled" setting.
     */
    public abstract boolean isEnabled();

    public void render(ScaledResolution sr) {
        if (!isEnabled()) {
            return;
        }
        NvgRenderer list = build(sr);
        if (list == null) {
            return;
        }
        Rect2f bounds = list.getBounds();
        movable.function.get(sr, bounds);
        list.bounds(bounds);
        list.flush(sr);
        postRender(list, sr, bounds);
    }

    public void update() {
    }
}
