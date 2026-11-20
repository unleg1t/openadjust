package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.settings.ExpandSetting;
import java.util.function.BooleanSupplier;
import com.adjustclient.core.gui.tabgui.KeyPressHandler;
import com.adjustclient.core.gui.tabgui.SettingNode;
import com.adjustclient.core.rendering.font.Fonts;

public final class ExpandComponent extends SettingNode<ExpandSetting> implements KeyPressHandler {

    public ExpandComponent(String label, ExpandSetting setting) {
        super(label, setting);
    }

    @Override
    public boolean keyPressed(int key) {
        return false;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        BooleanSupplier enabled = this.setting.enabledPredicate;
        dl.color(this.getTextColor(hovered, enabled == null || enabled.getAsBoolean()));
        dl.text(Fonts.TAHOMA_10, this.label + "...", x + 2.0F, y + 3.0F);
    }
}
