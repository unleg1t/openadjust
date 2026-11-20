package com.adjustclient.core.gui.clickgui.setting.string;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.managers.config.ConfigurationService;
import com.adjustclient.core.settings.StringSetting;
import com.adjustclient.core.gui.clickgui.setting.SettingComponent;
import com.adjustclient.utilities.Vec2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.TextInput;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.screen.AdjustScreen;

public final class StringEditorComponent extends SettingComponent<StringSetting> {

    public boolean layoutDirty;
    private boolean rebuildRequested;
    private TextInput[] inputs;
    private Float totalHeight;
    private boolean committed;

    public StringEditorComponent(StringSetting setting) {
        super(setting);
        this.inputs = new TextInput[0];
        this.totalHeight = 1.0F;
        this.rebuildRequested = true;                    // the inputs are built at the first layout
        Runnable onSettingChanged = () -> {
            if (this.committed) {
                this.committed = false;
            } else {
                this.rebuildRequested = true;
            }
        };
        setting.addListener((oldValue, newValue) -> onSettingChanged.run());
    }

    /** The row was laid out with another width (ClickGuiScreen.layoutTab): the inputs must be re-wrapped, unless the change came from this editor itself. */
    public void onWidthChanged() {
        if (this.committed) {
            this.committed = false;
        } else {
            this.rebuildRequested = true;
        }
    }

    private static List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>(List.of(text.split("\n", 0)));
        lines.removeIf(s -> s.trim().isEmpty());
        return lines;
    }

    /** Rebuilds one text input per string of the setting (text limited to the input's maximum length), then lays out. */
    private void rebuildInputs() {
        List<String> value = this.setting.getValue();
        this.inputs = new TextInput[value.size()];
        for (int i = 0; i < value.size(); i++) {
            TextInput input = new TextInput("...", Fonts.TAHOMA_10, this.setting.maxLength);
            input.field.setText(value.get(i));    // GuiTextField.setText: limited to the maximum length, cursor to the end
            this.inputs[i] = input;
        }
        this.layoutDirty = true;
        if (this.totalHeight != null) {
            this.layoutQuiet();
        } else {
            this.layoutQuiet();
            this.layoutQuiet();
        }
        if (ClickGuiScreen.INSTANCE != null) {
            ClickGuiScreen.INSTANCE.layoutDirty = true;
        }
    }

    void layout() {
        if (this.rebuildRequested) {
            this.rebuildRequested = false;
            this.rebuildInputs();
        } else if (this.layoutDirty && ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived) {
            float height = 1.0F;
            for (TextInput input : this.inputs) {
                input.setWidth((int) (this.bounds.width - 2.0F)); // wrap width
                int lines = input.lines.length;
                height += lines >= 2 ? (float) (lines - 1) * 7.5F + 8.5F : 8.5F;
            }
            this.totalHeight = height;
            this.layoutDirty = false;
        }
        if (ClickGuiScreen.INSTANCE != null) {
            ClickGuiScreen.INSTANCE.layoutDirty = true; // GUI root: relayout wanted
        }
    }

    void layoutQuiet() {
        if (this.rebuildRequested) {
            this.rebuildRequested = false;
            this.rebuildInputs();
            return;
        }
        if (!this.layoutDirty || !ConfigurationService.configurationsAvailable || !ConfigurationService.configurationReceived) {
            return;
        }
        float height = 1.0F;
        for (TextInput input : this.inputs) {
            input.setWidth((int) (this.bounds.width - 2.0F));
            int lines = input.lines.length;
            height += lines >= 2 ? (float) (lines - 1) * 7.5F + 8.5F : 8.5F;
        }
        this.totalHeight = height;
        this.layoutDirty = false;
    }

    /** Commit: unfocus every input, remember that the change comes from this editor and notify the setting's listeners. */
    private void commit() {
        for (TextInput input : this.inputs) {
            input.focused = false; // focused
        }
        this.committed = true;
        StringSetting setting = this.setting;
        setting.notifyListeners(setting.getValue(), setting.getValue());
    }

    @Override
    public boolean keyTyped(int typedChar, int keyCode) {
        int index = -1;
        TextInput focused = null;
        for (int i = 0; i < this.inputs.length; i++) {
            if (this.inputs[i].focused) { // focused
                index = i;
                focused = this.inputs[i];
                break;
            }
        }
        if (focused == null) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.commit();
            return true;
        }
        String text = focused.field.getText();
        if (keyCode == Keyboard.KEY_RETURN) {
            if (text.length() == 0) {
                return true; // Enter on an empty line does nothing
            }
            if (index == this.inputs.length - 1 && this.setting.maxEntries /* maxLines */ - 1 > index) {
                this.setting.getValue().add("");
                this.layout();
                TextInput last = this.inputs[this.inputs.length - 1];
                last.field.setFocused(true); // if (!focused) cursorCounter = 0; focused = true
                last.focused = true;
                return true;
            }
            this.commit();
            return true;
        }
        if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
            if (text.length() == 0 && this.setting.getValue().size() >= 2) {
                // empty line: remove it, focus the last input
                this.setting.getValue().remove(index);
                this.layout();
                TextInput last = this.inputs[this.inputs.length - 1];
                last.field.setFocused(true);
                last.focused = true;
                return true;
            }
        } else if (keyCode == Keyboard.KEY_V && GuiScreen.isCtrlKeyDown()) {
            List<String> lines = splitLines(com.adjustclient.utilities.ClipboardUtil.getClipboard() /* clipboard text */);
            if (lines.size() >= 2) {
                if (text.length() == 0) {
                    // multi-line paste: empty current line is replaced before appending the pasted lines
                    this.setting.getValue().remove(index);
                }
                this.setting.getValue().addAll(lines);
                this.layout();
                return true;
            }
        }
        // default: let the input handle the key, rewrap, store the text back into the list and keep focus on the rebuilt input
        focused.field.textboxKeyTyped((char) typedChar, keyCode);
        focused.setWidth((int) (this.bounds.width - 2.0F));
        if (index < this.setting.getValue().size()) {
            this.setting.getValue().set(index, focused.field.getText());
            this.layout();
            TextInput current = this.inputs[index];
            current.focused = focused.focused;
            current.animation = focused.animation;
        }
        return true;
    }

    @Override
    public void renderContent(com.adjustclient.core.events.Render2DEvent context, Vec2f mouse) {
        float x = this.bounds.x + 1.5F;
        float y = this.y + 4.0F;
        if (this.rebuildRequested) {
            this.rebuildRequested = false;
            this.rebuildInputs();
            this.layoutQuiet();
        } else {
            this.layoutQuiet();
        }
        NvgRenderer renderer = new NvgRenderer(new ArrayList<>(), new Stack<>(), -1L);
        for (TextInput input : this.inputs) {
            input.draw(renderer, x, y);
            int lines = input.lines.length;
            y += lines >= 2 ? (float) (lines - 1) * 7.5F + 8.5F : 8.5F;
        }
        renderer.flush(context != null && context.scaledResolution != null ? context.scaledResolution : AdjustScreen.screenResolution); // flush
        if (this.hovered /* hovered */) {
            RenderUtil.cursor = 221186; // GLFW_IBEAM_CURSOR
        }
    }

    /** Left press: focuses the input under the mouse (and unfocuses the others). Returns whether one was hit. */
    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (!pressed || button != 0 || (ClickGuiScreen.INSTANCE != null && ClickGuiScreen.INSTANCE.searchBar != null && ClickGuiScreen.INSTANCE.searchBar.active)) {
            return false;
        }
        float top = this.y + 2.0F;
        float left = this.x + 1.5F;
        boolean hit = false;
        for (TextInput input : this.inputs) {
            int lines = input.lines.length;
            float height = lines >= 2 ? (float) (lines - 1) * 7.5F : 0.0F;
            if (!hit && mouse.x >= left - 2.0F && left + this.bounds.width >= mouse.x && mouse.y >= top
                    && top + height + 8.5F >= mouse.y) {
                input.focused = true;
                hit = true;
            } else {
                input.focused = false;
                top += height + 8.5F;
            }
        }
        return hit;
    }

    /** A click outside the editor commits an edit in progress (unfocus everything and notify the setting's listeners). */
    @Override
    public void onClickOutside(Vec2f mouse, int button, boolean pressed) {
        if (pressed && button == 0) {
            this.commit();
        }
    }

    @Override
    public void expandSelf() {
        if (!this.setting.field45 && !this.expanded) {
            ClickGuiScreen.instance().markDirty();
            this.expanded = true;  // open
            this.refreshPending = true;  // changed
        }
    }

    @Override
    public float getHeight() {
        ClickGuiComponent parent = this.parent;
        if (parent != null && ClickGuiComponent.isFullyExpanded(parent) && this.rebuildRequested) {
            this.rebuildRequested = false;
            this.rebuildInputs();
        }
        return this.totalHeight;
    }

    @Override
    public void onUpdate() {
        for (TextInput input : this.inputs) {
            input.field.updateCursorCounter();
        }
    }
}
