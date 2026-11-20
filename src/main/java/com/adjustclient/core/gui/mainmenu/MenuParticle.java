package com.adjustclient.core.gui.mainmenu;

import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;

final class MenuParticle {
    Long lastUpdate;
    float x;
    float y;

    void draw(Vec2f mouse) {
        long now = System.currentTimeMillis();
        if (lastUpdate == null || now - lastUpdate >= 31L) {
            float elapsed = lastUpdate == null ? 1.0F : (float) (now - lastUpdate) / 30.0F;
            y += 0.5F * elapsed;
            lastUpdate = now;
        }
        float dx = mouse.x - (x + 0.375F);
        float dy = mouse.y - (y + 0.375F);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        int alpha = Math.min(255, Math.max(0, (int) (40.0F - distance * 0.2F)) + 40);
        NvgDraw.fillCircle(new Color(240, 240, 240, alpha).getRGB(), x, y, 0.75F);
    }
}
