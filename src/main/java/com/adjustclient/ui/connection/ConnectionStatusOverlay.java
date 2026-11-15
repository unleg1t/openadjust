package com.adjustclient.ui.connection;

import com.adjustclient.Adjust;
import com.adjustclient.backend.transport.HydraConnection;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.Timer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ConnectionStatusOverlay {
    private final Animation widthAnimation = new Animation(Easing.LINEAR, 160L);
    private final Animation heightAnimation = new Animation(Easing.LINEAR, 160L);
    private final Timer statusTimer = new Timer();
    private final Timer connectedTimer = new Timer(0L);
    private Animation fadeAnimation;

    public ConnectionStatusOverlay() {}

    public void draw(ScaledResolution resolution) {
        if (resolution == null || Fonts.TAHOMA_12 == null) {
            return;
        }
        GlStateManager.disableDepth();
        try {
            HydraConnection connection = Adjust.hydraConnection;
            boolean authenticated = connection != null && connection.authenticated;
            boolean ready = authenticated && connection.ready;
            if (ready) {
                if (this.connectedTimer.lastMs == 0L) {
                    this.connectedTimer.reset();
                }
                this.widthAnimation.duration = 110L;
                if (this.connectedTimer.hasElapsed(1900L) && this.fadeAnimation == null) {
                    this.fadeAnimation = new Animation(Easing.LINEAR, 160L);
                    this.fadeAnimation.value = 255.0F;
                    this.fadeAnimation.from = 255.0F;
                }
            }

            int alpha = 255;
            if (this.fadeAnimation != null) {
                alpha = (int) Math.max(0.0F, Math.min(255.0F, this.fadeAnimation.update(0.0F)));
                if (this.fadeAnimation.finished) {
                    ServiceUtil.connectionStatusOverlay = null;
                }
            }

            int grey = new Color(155, 155, 155, alpha).getRGB();
            Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            int accentArgb = ColorUtil.withAlpha(accent, alpha).getRGB();

            List<ConnectionStatusSegment> segments = new ArrayList<>();
            segments.add(new ConnectionStatusSegment(new TextComponent[] {
                new TextComponent(ready ? "Connected successfully to " : "Disconnected from ", grey),
                new TextComponent("Hydra", accentArgb),
                new TextComponent(ready ? ": Authenticated" : ": No connection", grey)
            }, Fonts.TAHOMA_12));

            if (authenticated) {
                this.statusTimer.reset();
            }
            if (!ready && !this.statusTimer.hasElapsed(2000L)) {
                segments.add(new ConnectionStatusLine(authenticated ? "Authenticating" : "Connecting"));
            }

            float y = 2.0F;
            float maxWidth = 0.0F;
            for (ConnectionStatusSegment segment : segments) {
                float segWidth = 0.0F;
                for (TextComponent component : segment.components) {
                    segWidth += segment.font.getStringWidth(component.text);
                }
                maxWidth = Math.max(maxWidth, segWidth + 6.0F);
                segment.y = y;
                y += segment.font.size + 3.0F;
            }

            float animatedWidth = this.widthAnimation.update(maxWidth);
            float animatedHeight = this.heightAnimation.update(y);
            float minHeight = 2.0F + segments.get(0).getSize().y;
            float drawHeight = Math.max(animatedHeight, minHeight);

            int boxAlpha = Math.max(10, Math.min(150, alpha));
            float boxX = resolution.getScaledWidth() / 2.0F - animatedWidth / 2.0F;
            float boxY = 30.0F;

            NvgDraw.fillRect(new Color(25, 25, 29, boxAlpha).getRGB(),
                    boxX, boxY, boxX + animatedWidth, boxY + drawHeight);
            RenderUtil.pushScissor(resolution, 1, boxX, boxY, animatedWidth, drawHeight);
            try {
                float centerX = boxX + animatedWidth / 2.0F;
                for (ConnectionStatusSegment segment : segments) {
                    float segWidth = 0.0F;
                    for (TextComponent component : segment.components) {
                        segWidth += segment.font.getStringWidth(component.text);
                    }
                    TextComponent.draw(segment.components, segment.font,
                            centerX - segWidth / 2.0F, boxY + segment.y + 2.0F);
                }
            } finally {
                RenderUtil.popScissor();
            }
        } finally {
            GlStateManager.enableDepth();
        }
    }
}
