package com.adjustclient.core.gui.clickgui;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import java.util.ArrayList;
import com.adjustclient.core.gui.clickgui.frame.HoverEffect;
import com.adjustclient.core.gui.clickgui.frame.ModuleEntry;
import com.adjustclient.core.gui.clickgui.frame.TabHeader;
import com.adjustclient.core.gui.screen.AdjustComponent;
import com.adjustclient.utilities.HashUtil;
import com.adjustclient.utilities.Vec2f;

public final class CategoryTab extends ClickGuiTab {
    public Category category;
    private Object unused76;

    public CategoryTab(Vec2f pos, ClickGuiScreen screen, Category category) {
        this.x = pos.x;
        this.y = pos.y;
        this.width = 66.0F;
        this.title = "";
        this.screen = screen;
        this.entries = new ArrayList<>();
        this.renderList = new ArrayList<>();
        this.rowList = new ArrayList<>();
        this.layoutList = new ArrayList<>();
        this.scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 135L);
        this.id = HashUtil.hash("mcat." + category.id);
        this.category = category;
    }

    public void init() {
        TabHeader header = new TabHeader(this);
        this.state = header;
        header.expanded = true;
        // fade modifier 1: alpha 0.4F -> 0.7F; its two lambdas capture (header, this)
        header.titleHover = new HoverEffect(header,
                () -> this.screen.draggedTab == this,
                HoverEffect.hoveredOf(header), 0.4F, 0.7F);
        header.modifiers.add(header.titleHover);   // modifiers = [fade1]
        header.gradientHover = new HoverEffect(header,
                () -> this.hovered,
                HoverEffect.hoveredOf(header), 0.5F, 1.0F);
        header.modifiers.add(header.gradientHover);   // modifiers = [fade1, fade2]
        this.entries.add(header);             // the header is the first entry of the tab
        for (Module module : category.getModules()) {
            ModuleEntry entry = new ModuleEntry(module);
            this.entries.add(entry);
            screen.componentMap.put(module, entry);
            ComponentFactory.build(this, entry, module.B_());
        }
        for (ClickGuiComponent e : this.entries) {
            e.width = this.width;
        }
    }
}
