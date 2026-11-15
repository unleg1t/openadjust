package com.adjustclient.ui.clickgui.setting.items;

import com.adjustclient.event.Render2DEvent;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.BlocksSetting;
import com.adjustclient.ui.clickgui.frame.Tooltip;
import com.adjustclient.ui.screen.AdjustComponent;
import com.adjustclient.util.Vec2f;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

public final class ItemChooserGrid extends ItemListEntry {

    private ItemListComponent owner;
    private com.adjustclient.setting.ItemsSetting setting;
    private ItemCell[] cells;
    private ItemCell hoveredCell;
    private Animation scroll;
    private float scrollTarget;

    public ItemChooserGrid(ItemListComponent owner, com.adjustclient.setting.ItemsSetting setting) {
        this.owner = owner;
        this.scroll = new Animation(Easing.EASE_OUT_EXPO, 150L);
        setting.addListener(() -> this.refresh());
        this.setting = setting;
        this.refresh();
    }

    Animation getFadeAnimation() {
        return this.scroll;
    }

    /** Clamps the scroll target: no scrolling for up to 20 entries, otherwise 0 .. (rows - 4) * 13 with rows = ceil(size / 5). */
    private void clampScroll() {
        int count = this.cells.length;
        if (count < 21) {
            this.scrollTarget = 0.0F;
            return;
        }
        int rows = (count + 4) / 5;
        float max = (float) (rows - 4) * 13.0F;
        if (this.scrollTarget > max) {
            this.scrollTarget = max;
        } else if (this.scrollTarget < 0.0F) {
            this.scrollTarget = 0.0F;
        }
    }

    void refresh() {
        this.hoveredCell = null;
        IntArrayList all = this.setting.availableIds;              // available ids
        it.unimi.dsi.fastutil.ints.IntList selected = this.setting.getValue();
        boolean browsing = this.owner.browsing;
        IntArrayList visible = new IntArrayList();
        (browsing
                ? all.intStream().filter(id -> !selected.contains(id))
                : selected.intStream())
                .filter(this.owner::matches)
                .forEach(visible::add);
        this.cells = new ItemCell[visible.size()];
        boolean blocks = this.setting instanceof BlocksSetting;
        for (int i = 0; i < visible.size(); i++) {
            int id = visible.getInt(i);
            ItemCell cell = new ItemCell();
            cell.width = 13.0F;                // width
            cell.height = 13.0F;                // height
            cell.owner = this.owner;
            cell.id = id;
            cell.hovered = false;
            String tooltipText;
            if (blocks) {
                Block block = Block.getBlockById(id);
                tooltipText = block.getLocalizedName();
                Item item = Item.getItemFromBlock(block);
                if (item != null) {
                    cell.stack = new ItemStack(item);
                } else if (ItemCell.SUBSTITUTES.containsKey(block)) {
                    Item substitute = (Item) ItemCell.SUBSTITUTES.get(block);
                    cell.stack = substitute == null ? new ItemStack((Item) null) : new ItemStack(substitute);
                } else {
                    cell.stack = new ItemStack(Item.getItemFromBlock(Blocks.anvil), 1, 100);
                }
            } else {
                cell.stack = this.owner.getStack(id);
                tooltipText = cell.stack.getDisplayName();
            }
            cell.tooltip = new Tooltip(this.owner, tooltipText,
                    () -> cell.hovered, 150L);
            this.cells[i] = cell;
        }
        this.clampScroll();
    }

    @Override
    public void draw(Vec2f mouse, Render2DEvent queue, boolean hovered) {
        float x2 = this.x + this.width;
        float y2 = this.y + this.height;
        NvgDraw.fillRect(new java.awt.Color(93, 93, 93, 30).getRGB(), this.x, this.y, x2, y2);
        this.drawCells(mouse, this.owner.hoveredEntry() == this, queue.scaledResolution);
    }

    private void drawCells(Vec2f mouse, boolean allowHover, ScaledResolution scaledResolution) {
        this.scroll.update(this.scrollTarget);
        this.hoveredCell = null;
        if (this.cells.length == 0) {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            String noun = this.setting instanceof BlocksSetting ? "blocks" : "items";
            String text;
            if (this.owner.searchText.length() == 0) {
                text = this.owner.browsing ? "No more " + noun + " available" : "No " + noun + " added";
            } else {
                text = "No matching " + noun;
            }
            Fonts.TAHOMA_10.drawString(text, this.x + 2.0F, this.y + 3.0F, 0xA0969696);
            return;
        }
        NvgContext.flushFrame();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // clip to the grid rectangle intersected with the current scissor (NvgDraw.peekScissor()), 0.5 extra at the bottom
        AdjustComponent scissor = NvgDraw.peekScissor();
        float baseY = this.y;
        float baseHeight = this.height + 0.5F;
        float clipTop;
        float clipHeight;
        if (scissor == null) {
            clipTop = baseY;
            clipHeight = baseHeight;
        } else if (scissor.getY() > baseY) {
            clipHeight = baseHeight - (scissor.getY() - baseY);
            clipTop = scissor.getY();
        } else if (baseY + baseHeight > scissor.getY() + scissor.height) {
            clipHeight = (scissor.getY() + scissor.height) - baseY;
            clipTop = baseY;
        } else {
            clipTop = baseY;
            clipHeight = baseHeight;
        }
        Render2DEvent localQueue = new Render2DEvent(null, 0.0F);
        RenderUtil.pushScissor(scaledResolution, 0, this.x, clipTop, this.width, clipHeight);
        float rowY = this.y - this.scroll.getValue();
        float colX = 0.5F;
        List<Runnable> deferred = new ArrayList<>();
        float clipBottom = clipTop + clipHeight;
        for (ItemCell cell : this.cells) {
            if (rowY > this.y - cell.height /* 13 */ && rowY <= clipBottom) {
                cell.x = colX + this.x;
                cell.y = rowY;
                cell.drawIcon();
                cell.queueHighlight(deferred);
                if (allowHover && cell.intersects(scissor, mouse.x, mouse.y)) {
                    this.hoveredCell = cell;
                    cell.hover(localQueue, mouse, true);
                } else {
                    cell.hover(localQueue, mouse, false);
                }
            }
            colX += cell.width; // + 13
            if (colX > 53.0F) {   // 5 columns: 0.5, 13.5, 26.5, 39.5, 52.5
                colX = 0.5F;
                rowY += cell.height;
                if (rowY >= this.y + this.height) {
                    break;
                }
            }
        }
        GL11.glPopAttrib();
        deferred.forEach(Runnable::run);
        NanoVG.nvgResetScissor(NvgContext.vg);
        localQueue.runPostTasks();
        NvgDraw.reapplyScissors();
    }

    /** Scroll by {@code delta} wheel steps of 13 units (called from {@link ItemListComponent#scrolled}); returns whether it changed. */
    public boolean scrollBy(float delta) {
        float old = this.scrollTarget;
        this.scrollTarget = old + delta * 13.0F;
        this.clampScroll();
        return old != this.scrollTarget;
    }

    void resetScroll() {
        this.scrollTarget = 0.0F;
        this.scroll.value = 0.0F;
        this.scroll.restart();
    }

    ItemCell getHoveredCell() {
        return this.hoveredCell;
    }
}
