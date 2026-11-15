package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.setting.StringSetting;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import com.adjustclient.render.font.TextInput;
import com.adjustclient.ui.tabgui.KeyEventHandler;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.ui.tabgui.TextInputNode;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.tabgui.TabGui;

public final class StringListEditorComponent extends SettingNode<StringSetting>
        implements KeyPressHandler, KeyEventHandler, TextInputNode {

    private TextInput[] fields;
    private final Animation scroll;
    private int selected;

    public StringListEditorComponent(StringSetting setting) {
        super(setting.field36 == null ? setting.name : setting.field36.get(), setting);
        this.scroll = new Animation(Easing.EASE_OUT_EXPO, 150L);
        this.label = this.label + ":";
        setting.addListener(() -> this.g());
        this.g();
    }

    @Override
    public boolean keyPressed(int key) {
        if (key != Keyboard.KEY_RIGHT && key != Keyboard.KEY_RETURN) {
            return false;
        }
        if (this.fields.length == 0) {
            return true;
        }
        this.selected = 0;
        TextInput first = this.fields[0];
        first.field.setFocused(true); // GuiTextField.setFocused (resets cursorCounter when it was not focused)
        first.focused = true;
        return true;
    }

    @Override
    public void keyEvent(Keyboard.KeyEvent event) {
        if (!event.state) {
            return;
        }
        int index = this.selected;
        TextInput current = this.fields[index];
        if (!current.focused) { // not focused
            return;
        }
        event.consumed = true;
        GuiTextField textField = current.field;
        int key = event.key;
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RETURN) {
            this.h();
            return;
        }
        if (key == Keyboard.KEY_DELETE || key == Keyboard.KEY_BACK) {
            if (textField.getText().isEmpty()) {
                List<String> values = this.setting.getValue();
                if (values.size() >= 2) {
                    values.remove(index);
                    this.g();
                    this.selected = Math.min(index, this.fields.length - 1);
                    this.focusSelected();
                    return;
                }
            }
        } else if (key == Keyboard.KEY_UP) {
            if (this.setting.getMaxEntries() < 2) {
                return;
            }
            current.focused = false;
            this.selected = index != 0 ? index - 1 : this.fields.length - 1;
            this.focusSelected();
            return;
        } else if (key == Keyboard.KEY_DOWN) {
            int limit = this.setting.getMaxEntries();
            if (limit < 2) {
                return;
            }
            current.focused = false;
            if (index != this.fields.length - 1) {
                this.selected = index + 1;
            } else if (index == limit - 1) {
                this.selected = 0;
            } else {
                this.setting.getValue().add("");
                this.g();
                this.selected = this.fields.length - 1;
            }
            this.focusSelected();
            return;
        }
        // any other key (and BACK / DELETE on a non-empty entry): type into the Minecraft text field
        textField.setFocused(true);
        textField.textboxKeyTyped(event.character, key);
        this.setting.getValue().set(index, current.field.getText()); // ArrayList.set
    }

    private void focusSelected() {
        TextInput field = this.fields[this.selected];
        field.field.setFocused(true);
        field.focused = true;
    }

    private void drawFields(NvgRenderer dl, int first, float x, float y) {
        for (int i = 0; i < this.fields.length; i++) {
            TextInput field = this.fields[i];
            if (i >= first - 1) { // one extra row above the first visible one
                field.draw(dl, x, y); // text field render
            }
            int lines = field.lines.length;
            if (Math.max(this.selected, 4) + 4 <= i) {
                return;
            }
            y += lines >= 2 ? (lines - 1) * 7.5F + 8.5F : 8.5F;
        }
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        com.adjustclient.ui.hud.tabgui.TabGuiSettings ctx = (com.adjustclient.ui.hud.tabgui.TabGuiSettings) context;
        int first = Math.max(this.selected - 4, 0);
        float rowBase = 8.5F;
        float hiddenAbove = 0.0F; // total height of the rows above the first visible one
        float visible = 0.0F;     // total height of the visible rows
        for (int i = 0; i < this.fields.length; i++) {
            TextInput field = this.fields[i];
            field.setWidth(90); // text field: set the wrap width (90)
            int lines = field.lines.length;
            float rowHeight = lines >= 2 ? (lines - 1) * 7.5F + rowBase : rowBase;
            if (first > i) {
                hiddenAbove += rowHeight;
            } else {
                visible += rowHeight;
            }
            if (i - first >= 8) {
                break;
            }
        }
        float panelHeight = visible + (float) ctx.rowHeight;
        if (!TabGui.layingOutNodes) {
            dl.color(((Color) ctx.owner).getRGB());
            dl.rect(x, y, width, panelHeight);
        }
        dl.color(Color.WHITE.getRGB());
        dl.text(Fonts.TAHOMA_10, this.label, x + 2.0F, y + 3.0F);
        if (!TabGui.layingOutNodes) {
            dl.pushClip(x, y + 8.0F, width, panelHeight - rowBase);
            this.scroll.update(hiddenAbove);
            this.drawFields(dl, first, x + 2.0F, (y + 11.0F) - this.scroll.value);
            dl.popClip();
        } else {
            this.drawFields(dl, first, x + 2.0F, (y + 11.0F) - this.scroll.value);
        }
    }

    public void e() {
        for (TextInput field : this.fields) {
            field.field.updateCursorCounter();
        }
    }

    private void h() {
        this.selected = 0;
        for (TextInput field : this.fields) {
            field.focused = false;
        }
        StringSetting setting = this.setting;
        setting.fireChanged();
    }

    private void g() {
        StringSetting setting = this.setting;
        List<String> values = setting.getValue();
        this.fields = new TextInput[values.size()];
        for (int i = 0; i < values.size(); i++) {
            TextInput field = new TextInput("...", Fonts.TAHOMA_10, setting.maxLength);
            field.field.setText(values.get(i));
            this.fields[i] = field;
        }
    }
}
