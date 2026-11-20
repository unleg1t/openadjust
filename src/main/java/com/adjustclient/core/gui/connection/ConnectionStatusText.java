package com.adjustclient.core.gui.connection;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.TextComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;

public class ConnectionStatusText {
    public List<ConnectionStatusRegion> regions;
    public ConnectionStatusRegion bounds;

    public ConnectionStatusText() {
        this(new ArrayList<>());
    }

    public ConnectionStatusText(List<TextComponent[]> lines) {
        this.regions = new ArrayList<>();
        float maxWidth = 0.0F;
        float totalHeight = 0.0F;
        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                TextComponent[] line = lines.get(i);
                ConnectionStatusRegion region = new ConnectionStatusRegion(Fonts.TAHOMA_13, line);
                this.regions.add(region);
                if (region.width > maxWidth) {
                    maxWidth = region.width;
                }
                totalHeight += region.height;
                if (i + 1 < lines.size()) {
                    totalHeight += 1.5F;
                }
            }
        }
        this.bounds = new ConnectionStatusRegion(Fonts.TAHOMA_13, new TextComponent[0]);
        this.bounds.width = maxWidth;
        this.bounds.height = totalHeight;
    }

    public void draw() {
        if (this.regions == null) {
            return;
        }
        for (ConnectionStatusRegion region : this.regions) {
            if (region.owner instanceof TextComponent[]) {
                TextComponent.draw((TextComponent[]) region.owner, Fonts.TAHOMA_13, region.x, region.y);
            }
        }
        GlStateManager.resetColor();
    }

    public void setPosition(float x, float y) {
        if (this.bounds == null || this.regions == null) {
            return;
        }
        this.bounds.x = x - this.bounds.width - 2.0F;
        float currentY = y - this.bounds.height - 2.0F;
        this.bounds.y = currentY;
        for (int i = 0; i < this.regions.size(); i++) {
            ConnectionStatusRegion region = this.regions.get(i);
            region.x = x - region.width - 2.0F;
            region.y = currentY;
            if (i + 1 < this.regions.size()) {
                currentY += region.height + 1.5F;
            }
        }
    }
}
