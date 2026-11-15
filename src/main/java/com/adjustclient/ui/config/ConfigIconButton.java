package com.adjustclient.ui.config;

import com.adjustclient.backend.api.request.config.UploadConfigurationRequest;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.util.SoundUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import java.awt.Color;

class ConfigIconButton extends Rect2f {
    private static final Timer VOTE_TIMER = new Timer(0L);
    public Animation animation;
    public Color color;
    public GlobalConfigEntry entry;
    public boolean upvote;
    private Object field36;

    public ConfigIconButton(GlobalConfigEntry entry, boolean upvote, Color color) {
        this.entry = entry;
        this.upvote = upvote;
        this.color = color;
        width = 12.0F;
        height = 12.0F;
    }

    public void draw(Vec2f mouse) {
        boolean hovered = mouse.x >= x && mouse.y >= y - 1.0F
                && mouse.x < x + width && mouse.y < y - 1.0F + height;
        boolean selected = entry.vote != null && entry.vote.booleanValue() == upvote;
        if (animation == null) animation = new Animation(Easing.LINEAR, 0L);
        animation.update(selected ? (hovered ? 1.0F : 0.88F) : (hovered ? 0.6F : 0.0F));
        animation.duration = 100L;
        Fonts.ICONS_12.drawString(upvote ? "h" : "i", x, y,
                ColorUtil.blend(color, new Color(140, 140, 140), animation.value).getRGB());
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (!pressed || mouse.x < x || mouse.y < y - 1.0F
                || mouse.x >= x + width || mouse.y >= y - 1.0F + height) return false;
        if (VOTE_TIMER.lastMs + 300L <= System.currentTimeMillis()) {
            if (entry.requestPending) return true;
            VOTE_TIMER.lastMs = System.currentTimeMillis();
            Boolean previous = entry.vote;
            Boolean next = previous == null || previous.booleanValue() != upvote ? Boolean.valueOf(upvote) : null;
            entry.setVote(next);
            if (!java.util.Objects.equals(previous, entry.vote)) {
                SoundUtil.play("party.message", 0.13F, next == null ? 0.33F : (next ? 1 : 0) * 0.03F + 0.27F);
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    private void vendorSendVote(Boolean previous, Boolean next) {
        entry.requestPending = true;
        new UploadConfigurationRequest(entry, entry.vote == null ? 2 : entry.vote ? 1 : 0)
                .toPacket().sendAsync(response -> {
                    if (response.status != ResponseStatus.OK) {
                        if (entry.vote != null) {
                            if (entry.vote) --entry.upvotes;
                            else --entry.downvotes;
                        }
                        entry.vote = previous;
                        if (previous != null) {
                            if (previous) ++entry.upvotes;
                            else ++entry.downvotes;
                        }
                    }
                    entry.requestPending = false;
                    VOTE_TIMER.lastMs = System.currentTimeMillis();
                });
    }
}
