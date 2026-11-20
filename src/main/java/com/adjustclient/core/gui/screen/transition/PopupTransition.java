package com.adjustclient.core.gui.screen.transition;

import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.gui.screen.AdjustScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public final class PopupTransition extends ScreenTransition {
    public Animation animation;
    public AdjustScreen screen;
    public boolean closing;
    public long closeDuration;
    public Object field24;

    public PopupTransition(AdjustScreen screen, long duration) {
        this(screen, new Animation(Easing.EASE_OUT_EXPO, duration), duration);
    }
    public PopupTransition(AdjustScreen screen, Animation animation, long duration) {
        this.screen = screen;
        this.animation = animation;
        this.closeDuration = duration;
    }
    public void close() {
        animation.duration = closeDuration;
        closing = true;
        if (screen != null && screen.blocksGameInteraction) {
            screen.blocksGameInteraction = false;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen != null) KeyBinding.unPressAllKeys();
            if (!Adjust.INSTANCE.screenStack.hasBlockingOverlay() && mc.currentScreen == null) mc.setIngameFocus();
        }
    }
    public boolean getProgress() {
        animation.update(closing ? 0.0F : 1.0F);
        if (!closing || animation.value != 0.0F) return false;
        if (screen != null) {
            screen.close();
            if (!screen.blocksGameInteraction) {
                screen.blocksGameInteraction = true;
                Minecraft.getMinecraft().setIngameNotInFocus();
            }
        }
        closing = false;
        return true;
    }
}
