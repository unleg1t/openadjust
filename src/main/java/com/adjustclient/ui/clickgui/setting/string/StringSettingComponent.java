package com.adjustclient.ui.clickgui.setting.string;

import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.setting.StringSetting;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.util.Vec2f;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.event.SettingChangeEvent;
import com.adjustclient.ui.clickgui.frame.ButtonBar;
import com.adjustclient.ui.clickgui.frame.ExpandButton;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.vfs.VirtualFileSystem;
import com.adjustclient.ui.hud.toggled.ToggledList;

public final class StringSettingComponent extends SettingComponent<StringSetting> {

    private HoverEffect hoverColor;
    private StringEditorComponent editor;

    public StringSettingComponent(StringSetting setting) {
        super(setting);
        BooleanSupplier always = () -> true;
        this.hoverColor = new HoverEffect(this, always, HoverEffect.hoveredOf(this), 0.5F, 0.8F);   // hover 0.5, active 0.8
        this.modifiers.add(this.hoverColor);
        ButtonBar bar = new ButtonBar(this);
        bar.buttons.add(new ExpandButton(this));
        this.modifiers.add(bar);
    }

    public void attachEditor(StringEditorComponent editor) {
        this.editor = editor;
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent context, Vec2f mouse) {
        StringSetting setting = this.setting;
        boolean allEmpty = true;                         // v71
        for (String line : setting.getValue()) {
            if (line.length() != 0) {
                allEmpty = false;
                break;
            }
        }
        String label = setting.getLabel();
        String counter;
        if (setting.maxEntries >= 2) {
            counter = setting.getValue().size() + "/" + setting.maxEntries; // sboutlining c.String(IAI)
        } else {
            counter = allEmpty ? "0" : "1";
        }
        NvgFontRenderer font = Fonts.TAHOMA_10;
        float centre = this.width / 2.0F + this.x;
        float width = font.getStringWidth(label + counter);
        float x = centre - width * 0.5F;
        float y = this.y + 4.0F;
        font.drawString(label, x, y, this.hoverColor.color(170));
        // the counter follows the label with a one unit gap (the end of the label is the return value of the drawString call)
        font.drawString(counter, x + font.getStringWidth(label) + 1.0F, this.y + 4.0F, this.hoverColor.color(130));
    }

    /** Opening for the first time prepares the editor (mark dirty, layout) before the normal open handling. */
    @Override
    public void setExpanded(boolean open) {
        if (!this.expanded && open && this.editor != null) {
            this.editor.layoutDirty = true;
            this.editor.layoutQuiet();
            this.editor.layout();
        }
        super.setExpanded(open);
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (!pressed || button != 0) {
            if (!pressed || button != 2) {
                return false;
            }
            // middle click: "Remove all text" confirmation
            DialogBuilder dialog = new DialogBuilder("Remove all text");
            dialog.message.append("Are you sure you want to clear ");
            dialog.message.italic = true;
            dialog.message.append(this.setting.getLabel());
            dialog.message.append(" and its content?");
            Consumer<DialogScreen> clear = d -> {
                StringSetting s = this.setting;
                List<String> value = s.getValue();
                value.clear();                                  // modCount++, size = 0, slots nulled
                ToggledList.INSTANCE.dirty = true;               // config dirty
                if (VirtualFileSystem.INSTANCE != null) {
                    VirtualFileSystem.INSTANCE.onSettingChange(new SettingChangeEvent(s, value));
                }
                if (s.listeners != null) {
                    for (Object listener : s.listeners) {              // onChange(list, list), each in try/catch
                        try {
                            ((com.adjustclient.setting.SettingChangeListener) listener).onChange(value, value);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true,
                                    "Error during property update of '{}': {}", new Object[] {s.name, t});
                        }
                    }
                }
                s.previousValue = null;
            };
            dialog.addButton("Clear", clear, DialogBuilder.ALWAYS_ENABLED);
            dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
            dialog.build();
            return true;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            this.openResetPrompt();
        } else {
            this.toggleExpanded();
        }
        return true;
    }

    @Override
    public void expandSelf() {
        if (this.setting.field45) {
            return;
        }
        if (!this.expanded) {
            this.setExpanded(true);
            this.refreshPending = true;
        }
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
