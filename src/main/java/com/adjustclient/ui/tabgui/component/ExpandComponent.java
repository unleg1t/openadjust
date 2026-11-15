package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.setting.ExpandSetting;
import java.util.function.BooleanSupplier;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.render.font.Fonts;

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
