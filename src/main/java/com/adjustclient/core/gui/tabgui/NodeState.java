package com.adjustclient.core.gui.tabgui;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;

public final class NodeState {

    /** The owning node. */
    public final GuiNode node;
    /**
     * Animation driving the vertical cursor offset of this node's children ("vertical animations", see
     * {@link #getRowOffset(int)}); created lazily, 115 ms.
     */
    public Animation rowAnimation;
    /** The parent node (null for the root); set by {@link GuiNode#addChild}. */
    public GuiNode parent;
    /**
     * Animation of the open/close ("expand") progress of this node, see {@link #getExpandProgress(boolean)};
     * created lazily.
     */
    public Animation expandAnimation;
    /** Index of the child that holds the cursor; may be out of range, always read through {@link #getSelectedIndex()}. */
    public int selectedIndex;
    /** True while the cursor is inside this node (its children are the active column). */
    public boolean expanded;

    public NodeState(GuiNode node) {
        this.node = node;
    }

    /**
     * Animated vertical offset (in rows) of the cursor: eases towards {@code target} over 115 ms and returns the
     * current value. Returns 0 without creating the animation while {@code target == 0}.
     */
    public float getRowOffset(int target) {
        if (this.rowAnimation == null) {
            if (target == 0) {
                return 0.0F;
            }
            this.rowAnimation = new Animation(Easing.EASE_OUT_EXPO, 115L);
        }
        return this.rowAnimation.update((float) target);
    }

    /**
     * The index of the selected child, normalised against the current number of children and written back:
     * a negative index wraps to the last child, an index past the end wraps to 0.
     */
    public int getSelectedIndex() {
        int size = this.node.getChildren().size();
        int index = this.selectedIndex;
        if (index < 0) {
            index = size - 1;
            this.selectedIndex = index;
        }
        if (size <= index) {
            this.selectedIndex = 0;
            return 0;
        }
        return index;
    }

    /**
     * Open/close progress (0..1) of this node: the duration of the animation is 380 ms while the node is
     * {@link #expanded}, otherwise 150 ms ({@code fast}) or 210 ms (not {@code fast}); the target is
     * {@code expanded ? 1 : 0}.
     *
     * @param fast selects the 150 ms duration for the collapsed state
     */
    public float getExpandProgress(boolean fast) {
        long duration = fast ? 150L : 210L;
        if (this.expandAnimation == null) {
            this.expandAnimation = new Animation(Easing.EASE_OUT_EXPO, 0L);
        }
        if (this.expanded) {
            duration = 380L;
        }
        this.expandAnimation.duration = duration;
        return this.expandAnimation.update(this.expanded ? 1.0F : 0.0F);
    }
}
