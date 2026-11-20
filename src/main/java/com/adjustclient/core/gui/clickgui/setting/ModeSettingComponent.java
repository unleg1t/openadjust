package com.adjustclient.core.gui.clickgui.setting;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.gui.clickgui.frame.HoverEffect;
import net.minecraft.util.ChatColor;

public class ModeSettingComponent extends SettingComponent<ModeSetting<?>> {

    HoverEffect labelColor;

    static final java.util.function.BooleanSupplier ALWAYS_ACTIVE = () -> true;

    public ModeSettingComponent(ModeSetting<?> setting) {
        super(setting);
        this.labelColor = new HoverEffect(this, ALWAYS_ACTIVE, HoverEffect.hoveredOf(this), 0.5F, 0.8F);
        this.modifiers.add(this.labelColor);
    }

    @Override
    public void renderContent(com.adjustclient.core.events.Render2DEvent queue, com.adjustclient.utilities.Vec2f mouse) {
        String label = super.getLabel() + ": ";
        int labelColorValue = this.labelColor.color(170);
        ModeOption mode = (ModeOption) this.setting.getValue();
        String modeName = mode.getName();
        String modeText = ChatColor.BOLD /* ChatColor control string */ + modeName;
        int modeColorValue = this.labelColor.color(190);
        TextComponent[] segments = {
                new TextComponent(label, labelColorValue),
                new TextComponent(modeText, modeColorValue)
        };
        TextComponent.draw(segments, Fonts.TAHOMA_10, this.bounds.x + 2.0F, this.y + 4.0F);
    }

    @Override
    public String getLabel() {
        java.util.function.Supplier<String> labelProvider =
                (java.util.function.Supplier<String>) this.setting.field36;
        String label = labelProvider == null ? this.setting.getName() : labelProvider.get();
        ModeOption mode = (ModeOption) this.setting.getValue();
        return label + ": " + mode.getName();
    }

    @Override
    public boolean onMousePressed(com.adjustclient.utilities.Vec2f mouse, int button, boolean pressed) {
        if (!pressed) {
            return false;
        }
        if (button == 0 && GuiScreen.isCtrlKeyDown()) {
            this.openResetPrompt();
            return true;
        }
        ModeSetting<ModeOption> setting = (ModeSetting<ModeOption>) this.setting;
        List<ModeOption> options = setting.getOptions();
        ModeOption current = (ModeOption) setting.getValue();
        if (button == 1) {
            int index = options.indexOf(current);
            ModeOption previous = index != 0 ? options.get(index - 1) : options.get(options.size() - 1);
            setting.setValue(previous);
            return true;
        }
        if (button != 0) {
            return false;
        }
        int index = options.indexOf(current);
        ModeOption next = index == options.size() - 1 ? options.get(0) : options.get(index + 1);
        setting.setValue(next);
        return true;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
