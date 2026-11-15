package com.adjustclient.ui.clickgui.setting.group;

import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.setting.Setting;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.frame.ButtonBar;
import com.adjustclient.ui.clickgui.frame.ExpandButton;
import com.adjustclient.ui.clickgui.frame.HeaderButton;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CollapsibleHeaderComponent extends SettingComponent<Setting<?>> {

    private Supplier<String> nameSource;
    private HoverEffect hoverColor;

    public CollapsibleHeaderComponent(Setting<?> setting, Supplier<String> nameSource) {
        super(setting);
        this.nameSource = nameSource;
        BooleanSupplier open = () -> ClickGuiComponent.isFullyExpanded(this);
        this.hoverColor = new HoverEffect(this, open, HoverEffect.hoveredOf(this), 0.3F, 1.0F);
        this.modifiers.add(this.hoverColor);
        ArrayList<HeaderButton> buttons = new ArrayList<>(2);
        buttons.add(new ExpandButton(this));                                               // adds its own hover child to modifiers
        this.modifiers.add(new ButtonBar(this, buttons));
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent context, Vec2f mouse) {
        String name = this.nameSource.get();
        NvgFontRenderer font = Fonts.TAHOMA_10;
        float hover = this.hoverColor.k().value; // the 95 ms animation is advanced by the modifier pass of the row
        int grey = Math.max(0, Math.min(255, (int) (110.0F * hover + 190.0F)));
        int alpha = Math.max(0, Math.min(255, (int) (48.0F * hover + 200.0F)));
        float x = this.width / 2.0F + this.x;
        float y = this.y + 4.0F;
        int color = new Color(grey, grey, grey, alpha).getRGB();
        font.drawString(name, x - font.getStringWidth(name) * 0.5F, y, color);
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (pressed && button == 0) {
            this.toggleExpanded();
            return true;
        }
        return false;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
