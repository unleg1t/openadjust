package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.font.image.NvgDraw;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ModifierPhase;
import com.adjustclient.ui.screen.AdjustComponent;
import com.adjustclient.util.Vec2f;

public final class ButtonBar extends ElementDecorator {

    protected ClickGuiComponent owner;
    public List<HeaderButton> buttons;
    protected boolean contentClipped;
    protected Object unused16;

    public ButtonBar(ClickGuiComponent owner, List<HeaderButton> buttons) {
        this.owner = owner;
        this.buttons = buttons;
        this.contentClipped = false;
        this.unused16 = null;
    }

    public ButtonBar(ClickGuiComponent owner, HeaderButton... buttons) {
        this(owner, new ArrayList<>(List.of(buttons)));
    }

    @Override
    public ModifierPhase getPhase() {
        return ModifierPhase.BOTH;
    }

    @Override
    public boolean mouseClicked(Vec2f pos, int button, boolean pressed) {
        if (button != 0 || !pressed) {
            return false;
        }
        for (HeaderButton b : this.buttons) {
            if (pos.x < b.x) {
                continue;
            }
            if (pos.y < b.y) {
                continue;
            }
            if (b.x + b.width <= pos.x) {
                continue;
            }
            if (b.y + b.height > pos.y) {
                b.onClick();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object drawList, Vec2f mouse, boolean layoutPass) {
        if (layoutPass) {
            ClickGuiComponent parent = this.owner;
            AdjustComponent bounds = parent.bounds;
            float right = bounds.x + bounds.width;           // bounds.x + bounds.width
            float parentY = parent.getY();
            List<Runnable> deferred = new ArrayList<>(1);
            float cursor = right - 0.5F;
            float totalWidth = 0.0F;
            for (HeaderButton b : this.buttons) {                    // modCount-checked iteration
                if (!b.isShown()) {
                    continue;
                }
                float width = b.width;
                b.x = cursor - width;                           // x
                float height = b.height;
                b.y = (11.0F - height) * 0.5F + parentY;        // y
                if (b.drawImmediately) {
                    b.render();
                } else {
                    deferred.add(b::render);
                }
                totalWidth += width;
                cursor = b.x;
            }
            if (!deferred.isEmpty()) {
                Runnable setupAndDrain = () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);   // SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO
                    for (Runnable task : deferred) {                        // the queued button renders
                        task.run();
                    }
                };
                queue.postTasks.add(setupAndDrain);
            }
            if (totalWidth > 0.0F) {
                this.contentClipped = true;
                // keep the owner's own content out of the area covered by the buttons
                NvgDraw.pushScissor(parent.x, 0.0F, parent.width - totalWidth, (float) window.getScaledHeight());   // window height
            }
        } else if (this.contentClipped) {
            NvgDraw.popScissor();
            this.contentClipped = false;
        }
    }
}
