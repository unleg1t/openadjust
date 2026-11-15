package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.font.NvgContext;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.screen.AdjustComponent;

public final class SettingSeparatorComponent extends ClickGuiComponent {

    Object obf56;

    public SettingSeparatorComponent() {
        super();
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        AdjustComponent bounds = this.bounds;
        if (bounds.height == 0.0F) {
            return;
        }
        float dx = bounds.x - mouse.x;
        float dy = bounds.y - mouse.y;
        float a = Math.max(0.0F, Math.min(1.0F, -((float) Math.sqrt(dx * dx + dy * dy) / 30.0F * 0.5F)));
        float sx = mouse.x - bounds.y;
        float sy = mouse.y - bounds.x;
        float strength = Math.max(0.15F, Math.min(0.3F, (float) Math.sqrt(sx * sx + sy * sy) / 31.0F));
        NvgContext.pushEvenScale();
        RenderUtil.drawAccentGradient(bounds.x, bounds.y, bounds.width, bounds.height, a, strength);
        NvgContext.popEvenScale();
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        return false;
    }

    @Override
    public float getHeight() {
        BooleanSetting separators = HUDModule.INSTANCE_ALT.clickGuiSeparators;
        return separators.getAsBoolean() ? 0.5F : 0.0F;
    }
}
