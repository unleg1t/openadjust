package com.adjustclient.ui.tabgui;

import com.adjustclient.render.NvgRenderer;
import java.util.ArrayList;

public abstract class GuiNode {

    /** Child nodes in display order. */
    public final ArrayList<GuiNode> children;
    /** Per-node layout / animation / selection state; {@code state.node == this}. */
    protected final NodeState state;

    protected GuiNode() {
        this.children = new ArrayList<>();
        this.state = new NodeState(this);
    }

    public final void addChild(GuiNode child) {
        child.state.parent = this;
        this.children.add(child);
    }

    public abstract void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width);

    /**
     * Whether this row is currently hidden (the setting's {@code hideIf} dependency says so). Hidden nodes
     * are skipped by layout, hit testing and keyboard navigation. Default: {@code false}
     * ({@link SettingNode} overrides it with the setting's dependency).
     */
    public boolean isHidden() {
        return false;
    }

    public ArrayList<GuiNode> getChildren() {
        return this.children;
    }
}
