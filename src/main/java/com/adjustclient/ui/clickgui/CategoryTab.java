package com.adjustclient.ui.clickgui;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import java.util.ArrayList;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.clickgui.frame.ModuleEntry;
import com.adjustclient.ui.clickgui.frame.TabHeader;
import com.adjustclient.ui.screen.AdjustComponent;
import com.adjustclient.util.HashUtil;
import com.adjustclient.util.Vec2f;

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
