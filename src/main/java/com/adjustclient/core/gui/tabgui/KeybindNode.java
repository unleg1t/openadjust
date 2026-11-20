package com.adjustclient.core.gui.tabgui;

import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.rendering.NvgRenderer;
import java.util.function.BooleanSupplier;
import net.minecraft.util.ChatColor;
import com.adjustclient.core.bind.BindKey;
import com.adjustclient.core.gui.tabgui.component.KeybindAddComponent;
import com.adjustclient.core.gui.tabgui.component.KeybindKeyComponent;
import com.adjustclient.core.rendering.font.Fonts;

public final class KeybindNode extends LabelNode implements BindRefreshable {

    /** Prefix of the text: the setting name / "Keybind". */
    private final String name;
    private final Bind bind;
    private final KeybindAddComponent addEntry;
    /** Visibility dependency of the setting; null for the module keybind row. */
    private final BooleanSupplier hideIf;

    public KeybindNode(String name, Bind bind, BooleanSupplier hideIf) {
        this.name = name;
        this.bind = bind;
        this.hideIf = hideIf;
        this.addEntry = new KeybindAddComponent(bind);
        this.rebuild();
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        boolean active = !this.bind.keys.isEmpty();
        dl.color(this.getTextColor(hovered, active));
        dl.text(Fonts.TAHOMA_10, this.label, x + 2.0F, y + 3.0F);
    }

    @Override
    public boolean isHidden() {
        if (this.hideIf == null) {
            return false;
        }
        return this.hideIf.getAsBoolean();
    }

    @Override
    public java.util.ArrayList<GuiNode> getChildren() {
        return this.children;
    }

    @Override
    public void onBindChanged(Object event) {
        if (this.bind == ((com.adjustclient.core.events.BindChangeEvent) event).bind) {
            this.rebuild();
            return;
        }
    }

    /**
     * Rebuilds children and text: one {@code k} node per key of the bind (named after the key), then the
     * "Add keybind..." node; the text becomes {@code name + ": " + ChatColor + summary} where the summary is
     * "None" (no key), the key name (one key) or "&lt;n&gt;..." (n keys).
     */
    public void rebuild() {
        this.children.clear();
        for (BindKey key : this.bind.keys) {
            String keyName = key.getName();
            this.addChild(new KeybindKeyComponent(keyName, this.bind, key));
        }
        this.addChild(this.addEntry);

        int size = this.children.size();
        String summary;
        if (size == 1) {
            summary = "None";
        } else if (size == 2) {
            summary = ((KeybindKeyComponent) this.children.get(0)).key.getName();
        } else {
            summary = (size - 1) + "...";
        }
        this.label = this.name + ": " + ChatColor.BOLD + summary;
    }
}
