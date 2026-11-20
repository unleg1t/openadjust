package com.adjustclient.core.gui.clickgui;

import com.adjustclient.core.rendering.font.image.NvgDraw;
import net.minecraft.client.gui.ScaledResolution;
import java.util.ArrayList;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.gui.clickgui.frame.ButtonBar;
import com.adjustclient.core.gui.clickgui.frame.ElementDecorator;
import com.adjustclient.core.gui.clickgui.frame.ExpandButton;
import com.adjustclient.core.gui.clickgui.frame.HeaderButton;
import com.adjustclient.core.gui.clickgui.frame.HoverBackground;
import com.adjustclient.core.gui.clickgui.frame.HoverEffect;
import com.adjustclient.core.gui.clickgui.frame.ModuleEntry;
import com.adjustclient.core.gui.clickgui.frame.TabHeader;
import com.adjustclient.core.gui.clickgui.frame.Tooltip;
import com.adjustclient.core.gui.clickgui.setting.ExpandSettingComponent;
import com.adjustclient.core.gui.screen.AdjustComponent;
import com.adjustclient.utilities.Vec2f;

public abstract class ClickGuiComponent extends AdjustComponent {
    public ArrayList<Modifier> modifiers;
    protected ClickGuiComponent parent;
    public AdjustComponent bounds;
    public ClickGuiTab owner;
    public ArrayList<ClickGuiComponent> children;
    protected int backgroundColor;
    protected int visibilityStamp;
    protected boolean hovered;
    protected boolean expanded;
    protected boolean shown;
    protected boolean refreshPending;
    protected boolean cachedVisible;

    protected ClickGuiComponent() {
        this.modifiers = new ArrayList<>();
        this.bounds = new AdjustComponent();
        this.children = new ArrayList<>();
        this.visibilityStamp = -1;
        this.cachedVisible = false;
    }

    public void onScreenClosed() {
    }

    public void renderContent(Render2DEvent queue, Vec2f mouse) {
    }

    public String getSearchText() {
        return null;
    }

    public boolean computeVisible(int stamp) {
        return isVisible(stamp);
    }

    public boolean isVisible(int stamp) {
        if (stamp == this.visibilityStamp) {
            return this.cachedVisible;
        }
        this.visibilityStamp = stamp;
        if (this.parent != null) {
            this.cachedVisible = this.parent.computeVisible(stamp);
        } else {
            this.cachedVisible = true;
        }
        return this.cachedVisible;
    }

    /** Key handler (typedChar, keyCode): offers the key to the modifiers of this component in list order; the first one that consumes it wins. */
    public boolean keyTyped(int typedChar, int keyCode) {
        if (modifiers == null || modifiers.isEmpty()) {
            return false;
        }
        for (Modifier m : new ArrayList<>(modifiers)) {
            if (m.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean onScroll(Vec2f mouse, float direction) {
        return false;
    }

    /** Mouse button handler (mouse, button, pressed): lets the modifiers consume the click first; a right click toggles the expansion. */
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        for (Modifier m : modifiers) {
            if (m.mouseClicked(mouse, button, pressed)) {
                return true;
            }
        }
        if (this.onMousePressed(mouse, button, pressed)) {
            return true;
        }
        if (button == 1) {
            if (pressed) {
                toggleExpanded();
                return true;
            }
            return false;
        }
        return false;
    }

    protected boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        return false;
    }

    public void render(Render2DEvent queue, Render2DEvent drawList, Vec2f mouse, ScaledResolution window) {
        ArrayList<Modifier> deferred = null;
        for (Modifier m : modifiers) {
            ModifierPhase phase = m.getPhase();
            if (phase == ModifierPhase.PRE) {
                m.render(window, queue, drawList, mouse, true);
            } else {
                if (deferred == null) {
                    deferred = new ArrayList<>();
                }
                deferred.add(m);
                if (phase != ModifierPhase.POST) {   // BOTH
                    m.render(window, queue, drawList, mouse, true);
                }
            }
        }
        this.renderContent(queue, mouse);
        if (deferred != null) {
            for (Modifier m : deferred) {
                m.render(window, queue, drawList, mouse, false);
            }
        }
    }

    /**
     * Expands / collapses the component. Marks the screen dirty (screen = ClickGuiScreen.instance(), created lazily), stores the
     * flag, clears the refresh-pending flag and, when collapsing, walks up the parent chain (while every ancestor is expanded, using
     * {@link #isFullyExpanded()}) to reset the state of collapsed groups (tab header animation target 1.0F / 0).
     */
    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) {
            return;
        }
        ClickGuiScreen.instance().layoutDirty = true;
        this.expanded = expanded;
        this.refreshPending = false;
        if (expanded) {
            return;
        }
        if (this.parent == null || this.parent.isFullyExpanded()) {
            return;
        }
        for (Modifier m : this.modifiers) {
            if (m instanceof HoverEffect || m instanceof HoverBackground) {
                m.reset();
            } else if (m instanceof ButtonBar) {
                for (HeaderButton b : ((ButtonBar) m).buttons) {
                    if (!(b instanceof ExpandButton)) {
                        continue;
                    }
                    ExpandButton btn = (ExpandButton) b;
                    ClickGuiComponent p = btn.parent;
                    boolean open;
                    if (p.owner != null && p.owner.screen.searchBar.isSearching()) {
                        open = p.owner.screen.searchBar.hasMatchingChild(p);
                    } else {
                        open = p.isFullyExpanded();
                    }
                    btn.fade.value = open ? 1.0F : 0.0F;
                    btn.fade.restart();
                }
            }
        }
        if (this instanceof ExpandSettingComponent) {
            ExpandSettingComponent esc = (ExpandSettingComponent) this;
            esc.expandAnimation.value = 0.0F;
            esc.expandAnimation.restart();
        }
    }

    public void onClickOutside(Vec2f mouse, int button, boolean pressed) {
    }

    public boolean isHovered() {
        return hovered;
    }

    /** Expands the component if it is collapsed and flags it for refresh. */
    public void expandSelf() {
        if (expanded) {
            return;
        }
        setExpanded(true);
        refreshPending = true;
    }

    /**
     * Toggles the expansion (right click on a row). Outside of the search (closed, or open with an empty query) the state is simply
     * inverted; during a search a temporarily opened row closes again, otherwise the row and its descendants are pinned into the
     * result ({@link SearchBar#collect}) and the ancestors of the children are expanded ({@link #expandAncestors()}).
     */
    public void toggleExpanded() {
        boolean target = !expanded;
        ClickGuiTab tab = this.owner;
        if (tab == null) {
            setExpanded(target);
            return;
        }
        ClickGuiScreen screen = tab.screen;
        SearchBar search = screen.searchBar;
        if (!search.isSearching()) {
            setExpanded(target);
            return;
        }
        tab.screen.layoutDirty = true;
        if (refreshPending) {
            setExpanded(false);
            return;
        }
        search.collect(this);
        for (ClickGuiComponent c : children) {
            c.expandAncestors();
            for (ClickGuiComponent child : c.children) {
                child.expandAncestors();
            }
        }
        this.expandAncestors();
    }

    public void onUpdate() {
    }

    /** true for the tab header row (it is never scrolled and is always listed). */
    public boolean isHeader() {
        return false;
    }

    public float getHeight() {
        return 0.0F;
    }

    public float measureHeight() {
        return getHeight();
    }

    public void expandAncestors() {
        ClickGuiComponent p = this;
        while (p.parent != null) {
            p.parent.expandSelf();
            p = p.parent;
        }
        if (this instanceof ExpandSettingComponent && !this.expanded) {
            setExpanded(true);
            this.refreshPending = true;
        }
        if (this.owner != null) {
            ClickGuiComponent state = this.owner.getState();
            if (state != null && !state.expanded) {
                state.setExpanded(true);
                this.refreshPending = true;
            }
        }
    }

    /** Re-parents this component: removes it from the old parent's children and appends it to the new parent's children. */
    public ClickGuiComponent setParent(ClickGuiComponent newParent) {
        if (this.parent != null) {
            this.parent.children.remove(this); // equals() based removal (first match), array shifted left
        }
        newParent.children.add(this);
        this.parent = newParent;
        return this;
    }

    public static boolean isFullyExpanded(ClickGuiComponent c) {
        for (ClickGuiComponent p = c; ; p = p.parent) {
            if (!p.expanded) {
                return false;
            }
            if (p.parent == null) {
                return true;
            }
        }
    }

    public boolean isFullyExpanded() {
        return isFullyExpanded(this);
    }
}
