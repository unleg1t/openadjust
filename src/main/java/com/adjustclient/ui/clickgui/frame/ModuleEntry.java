package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.font.Fonts;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.command.impl.ConfigCommand;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.overlay.BindOverlay;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.util.Vec2f;

public final class ModuleEntry extends ClickGuiComponent {

    protected final com.adjustclient.module.Module module;
    protected final HoverEffect labelHover;
    protected Object unused64;
    protected int animationTarget;

    public ModuleEntry(com.adjustclient.module.Module module) {
        super();
        this.module = module;
        module.field20 = this;
        this.modifiers.add(new HoverBackground(this));
        this.labelHover = new HoverEffect(this, () -> (module.flags & 2) != 0, HoverEffect.hoveredOf(this), 0.333F, 1.0F);
        this.modifiers.add(this.labelHover);
        String description = module.getDescription();
        if (description != null) {
            this.modifiers.add(new Tooltip(this, 300L, description, null));
        }
        String additionalDescription = module.getAdditionalDescription();
        if (additionalDescription != null) {
            this.modifiers.add(new Tooltip(this, 900L, additionalDescription, null));
        }
        ArrayList<HeaderButton> buttons = new ArrayList<HeaderButton>();
        if (!module.getSettings().isEmpty()) {
            buttons.add(new ExpandButton(this));
        }
        HoverEffect visibilityHover = new HoverEffect(this, () -> (module.flags & 4) != 0, HoverEffect.hoveredOf(this), 0.05F, 1.0F);
        this.modifiers.add(visibilityHover);
        buttons.add(new VisibilityButton(visibilityHover, this));
        this.modifiers.add(new ButtonBar(this, buttons));
    }

    @Override
    public void renderContent(Render2DEvent queue, Vec2f mouse) {
        Fonts.TAHOMA_12.drawString(this.module.name, this.x + 2.5F, this.y + 4.5F, this.labelHover.color(190));
    }

    @Override
    public String getSearchText() {
        return this.module.name;
    }

    @Override
    public void setExpanded(boolean expanded) {
        boolean collapsing = !expanded && this.expanded;
        super.setExpanded(expanded);
        if (collapsing) {
            this.collapseAll(this.children);
        }
    }

    private void collapseAll(ArrayList<ClickGuiComponent> list) {
        if (list == null) {
            return;
        }
        for (ClickGuiComponent c : list) {
            c.setExpanded(false);
            this.collapseAll(c.children);
        }
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (button == 0 && pressed) {
            if (!GuiScreen.isCtrlKeyDown()) {
                boolean enabled = (this.module.flags & 2) != 0;
                this.module.toggle(!enabled);
                return true;
            }
            DialogBuilder dialog = new DialogBuilder("Reset module " + this.module.name + " to default?");
            dialog.message.append("The settings along with enabled and visible states of ");
            dialog.message.italic = true;                                        // emphasised module name
            dialog.message.append(this.module.name);
            dialog.message.append(" will be reset to their defaults if you continue.");
            Consumer<DialogScreen> reset = result -> this.resetModule();
            Function<DialogScreen, Boolean> style = DialogBuilder.ALWAYS_ENABLED;
            Consumer<DialogScreen> noop = result -> { };
            dialog.addButton("Cancel", noop, style);
            dialog.addButton("Reset Module", reset, style);
            dialog.build();
            return true;
        }
        if (button == 2 && pressed) {
            ClickGuiScreen screen = ClickGuiScreen.instance();                      // ClickGuiScreen singleton (created on demand)
            Bind bind = this.module.bind;
            Consumer<Object> addKey = key -> bind.addKey((com.adjustclient.bind.BindKey) key);
            BindOverlay overlay = BindOverlay.create(this.module.name, addKey, bind.keys);
            screen.addOverlay(overlay);
            return true;
        }
        return false;
    }

    private void resetModule() {
        com.adjustclient.module.Module m = this.module;
        byte flags = m.flags;
        boolean visible = (flags & 4) != 0;
        boolean defaultVisible = (m.savedFlags & 4) != 0;
        if (visible != defaultVisible) {
            flags = (byte) (defaultVisible ? flags | 4 : flags & 0xFB);
            m.flags = flags;
            if (Adjust.INSTANCE != null && Adjust.INSTANCE.fileSystem != null) {
                Adjust.INSTANCE.fileSystem.markDirty();
            }
        }
        flags = m.flags;
        m.flags = (byte) (((m.savedFlags & 8) != 0) ? flags | 8 : flags & 0xF7);
        m.toggle((m.savedFlags & 2) != 0);
        ConfigCommand.resetSettings(m);                                          // reset every setting of the module to its default
    }

    @Override
    public float getHeight() {
        return 11.0F;
    }
}
