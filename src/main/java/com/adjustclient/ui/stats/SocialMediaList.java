package com.adjustclient.ui.stats;

import com.adjustclient.hypixel.api.model.player.HypixelSocialLinks;
import com.adjustclient.render.font.TextRenderer;
import net.minecraft.client.gui.GuiButton;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

final class SocialMediaList extends StatsList {
    private final HypixelStatsScreen owner;

    SocialMediaList(HypixelStatsScreen screen) { super(screen); owner = screen; }

    @Override protected void drawSlot(int index, int x, int y, int height, int mouseX, int mouseY) {
        Pair<String, String> row = owner.rows.get(index);
        TextRenderer font = mc.fontRendererObj;
        int color = (index & 1) == 0 ? 0xFFFFFF : 9474192;
        font.drawString(row.getLeft(), x - 48, y + 1, color, 1.0F);
        font.drawString(row.getRight(), (int) (x + 215 - font.getStringWidth(row.getRight(), 1.0F) + 50.0F), y + 1, color, 1.0F);
    }

    @Override public void populate() {
        for (GuiButton button : owner.buttons) {
            if (button == owner.socialButton) button.enabled = false;
            else button.enabled = !owner.isUnavailable(button);
        }
        if (owner.player.socialMedia == null || owner.player.socialMedia.links == null) return;
        HypixelSocialLinks links = owner.player.socialMedia.links;
        if (links.youtube != null) owner.rows.add(new ImmutablePair<>("YouTube", links.youtube));
        if (links.twitter != null) owner.rows.add(new ImmutablePair<>("Twitter", links.twitter));
        if (links.discord != null) owner.rows.add(new ImmutablePair<>("Discord", links.discord));
    }

    @Override public String getName() { return "Social Media"; }
}
