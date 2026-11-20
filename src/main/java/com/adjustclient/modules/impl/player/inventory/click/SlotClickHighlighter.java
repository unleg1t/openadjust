package com.adjustclient.modules.impl.player.inventory.click;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.events.InventoryKeyEvent;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

public final class SlotClickHighlighter implements Listener {
    private final Int2ObjectOpenHashMap<Highlight> highlights = new Int2ObjectOpenHashMap<>();
    private GuiContainer screen;

    public void highlight(int slot) { highlights.put(slot, new Highlight()); }

    @EventHandler public void onSlotRender(com.adjustclient.core.events.SlotRenderEvent event) {
        GuiContainer current = event.screen;
        if (!Objects.equals(screen, current)) {
            screen = current;
            highlights.clear();
        }
        Slot slot = event.slot;
        Highlight entry = highlights.get(slot.slotNumber);
        if (entry == null) return;
        float expansion = entry.expansion.update(210.0F);
        int alpha = (int) expansion / 2;
        if (expansion > 105.0F && entry.fade == null) {
            entry.fade = new Animation(Easing.EASE_IN_QUAD, 350L);
            entry.fade.value = alpha;
            entry.fade.startedAt = System.currentTimeMillis();
            entry.fade.from = entry.fade.value;
            entry.fade.finished = false;
        }
        if (entry.fade != null) {
            float fade = entry.fade.update(0.0F);
            if (fade == 0.0F) { highlights.remove(slot.slotNumber); return; }
            alpha = (int) fade;
        }
        float x = slot.xDisplayPosition + event.guiLeft;
        float y = slot.yDisplayPosition + event.guiTop;
        NvgDraw.pushScissor(x, y, 16.0F, 16.0F);
        NvgDraw.fillCircle((alpha << 24) | 0xC8C8C8, x + 8.0F, y + 8.0F, 0.057F * entry.expansion.value);
        NvgDraw.popScissor();
    }

    public static final class Highlight {
        public final Animation expansion = new Animation(Easing.EASE_OUT_SINE, 330L);
        public Animation fade;
    }
}
