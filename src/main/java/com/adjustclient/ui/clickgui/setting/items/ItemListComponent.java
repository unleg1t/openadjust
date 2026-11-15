package com.adjustclient.ui.clickgui.setting.items;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.util.Vec2f;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Keyboard;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.font.TextInput;

public class ItemListComponent extends SettingComponent<com.adjustclient.setting.ItemsSetting> {

    boolean browsing;
    private ItemListEntry[] entries;
    private ItemChooserGrid grid;
    public Consumer<Integer> hotbarTarget;
    private TextInput search;
    String searchText = "";
    private ItemListEntry hoveredEntry;

    protected ItemListComponent(com.adjustclient.setting.ItemsSetting setting, boolean withControls) {
        super(setting);
        this.searchText = "";
        this.browsing = false;
        this.modifiers.clear();
        this.search = withControls ? new TextInput("Search...", com.adjustclient.render.font.Fonts.TAHOMA_10, 32) : null;
        this.grid = new ItemChooserGrid(this, setting);
        ItemListButton button = withControls ? new ItemListButton(this) : null;
        this.entries = java.util.stream.Stream.of(this.grid, button)
                .filter(entry -> entry != null)
                .toArray(ItemListEntry[]::new);
    }

    /** The items / blocks setting row (factory b_qDT0SUXcymBfhGqM7FfyCD, called by ComponentFactory.build). */
    public ItemListComponent(com.adjustclient.setting.ItemsSetting setting) {
        this(setting, true);
    }

    public ItemChooserGrid getGrid() {
        return this.grid;
    }

    public TextInput getSearch() {
        return this.search;
    }

    public com.adjustclient.setting.ItemsSetting getSetting() {
        return this.setting;
    }

    ItemListEntry hoveredEntry() {
        return this.hoveredEntry;
    }

    /** Switches between "added" and "available" view: refreshes the grid, resets the scroll and restarts its animation. */
    void toggleBrowse() {
        this.browsing = !this.browsing;
        this.grid.refresh();
        this.grid.resetScroll(); // scrollTarget = 0, animation value = 0, restart (startTime = now, from = value, finished = false)
    }

    /** Wraps the search input to the row width (at least 8) and asks the frame to relayout when the row height changed. */
    void relayout() {
        float oldHeight = this.search.lineCount() >= 2 ? (float) (this.search.lineCount() - 1) * 7.5F + 9.5F + 52.0F : 61.5F;
        this.search.setWidth(Math.max((int) this.bounds.width - 2, 8)); // wrap width
        float newHeight = this.search.lineCount() >= 2 ? (float) (this.search.lineCount() - 1) * 7.5F + 9.5F + 52.0F : 61.5F;
        if (oldHeight != newHeight && this.owner != null) {
            this.owner.screen.layoutDirty = true; // frame needs relayout
        }
    }

    /** Clears the search (unfocus, empty text, empty filter) and rebuilds the grid. */
    @Override
    public void onScreenClosed() {
        if (this.search == null) {
            return;
        }
        this.search.focused = false; // focused
        this.search.field.setText("");
        this.searchText = "";
        this.relayout();
        this.grid.refresh();
        this.grid.resetScroll();
    }

    @Override
    public boolean isVisible(int stamp) {
        boolean visible = super.isVisible(stamp);
        if (this.search != null && (!visible || (this.parent != null && !this.parent.isFullyExpanded()))) {
            this.search.focused = false;
        }
        return visible;
    }

    @Override
    public boolean keyTyped(int typedChar, int keyCode) {
        if (this.search == null || !this.search.focused) {
            return false;
        }
        if (ClickGuiScreen.instance().searchBar.active) { // GUI root: modal state
            this.search.focused = false;
            return false;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
            this.search.focused = false;
            return true;
        }
        this.search.field.setFocused(true);
        this.search.field.textboxKeyTyped((char) typedChar, keyCode);
        String text = this.search.field.getText();
        if (!text.equals(this.searchText)) {
            this.searchText = text.trim().toLowerCase(Locale.ROOT);
            this.relayout();
            this.grid.refresh();
            this.grid.resetScroll();
        }
        return true;
    }

    /** Wheel over the grid scrolls it by 13 per step (see {@link ItemChooserGrid#scrollBy}); true when the offset changed. */
    @Override
    public boolean onScroll(Vec2f mouse, float delta) {
        ItemChooserGrid g = this.grid;
        if (mouse.x < g.x || mouse.y < g.y || g.x + g.width <= mouse.x || g.y + g.height <= mouse.y) {
            return false;
        }
        return g.scrollBy(delta);
    }

    /**
     * Layout + draw. Search input at (bounds.x + 1.5, y + 4.0) via a fresh NvgRenderer; hover over the input shows the I-beam cursor.
     * Entries are laid out from the rows: button 8 x 8 at (x + w - 10, y + h - 10); grid at (x, y + inputHeight, w, h - inputHeight)
     * with inputHeight = 9.5 (+ 7.5 per extra input line). The hovered entry is the last one (top most) containing the mouse.
     */
    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent context, Vec2f mouse) {
        boolean overSearch = false;
        if (this.search != null) {
            this.relayout();
            overSearch = this.hovered /* hovered */ && this.hitSearch(mouse);
            this.search.hovered = overSearch;
            NvgRenderer renderer = new NvgRenderer(new ArrayList<>(), new Stack<>(), -1L);
            this.search.draw(renderer, this.bounds.x + 1.5F, this.y + 4.0F);
            renderer.flush(context != null && context.scaledResolution != null
                    ? context.scaledResolution
                    : com.adjustclient.ui.screen.AdjustScreen.screenResolution);
            if (overSearch) {
                RenderUtil.cursor = 221186; // GLFW_IBEAM_CURSOR
            }
        }
        this.hoveredEntry = null;
        float inputHeight = this.search == null ? 0.0F
                : (this.search.lineCount() >= 2 ? (float) (this.search.lineCount() - 1) * 7.5F + 9.5F : 9.5F);
        ItemListEntry hit = null;
        for (int i = this.entries.length - 1; i >= 0; i--) {
            ItemListEntry entry = this.entries[i];
            if (entry instanceof ItemListButton) {
                entry.x = (this.x + this.width) - 8.0F - 2.0F;
                entry.y = (this.y + this.height) - 8.0F - 2.0F;
                entry.width = 8.0F;
                entry.height = 8.0F;
            } else {
                entry.x = this.x;
                entry.y = inputHeight + this.y;
                entry.width = this.width;
                entry.height = this.height - inputHeight;
            }
            if (hit == null && mouse.x >= entry.x && mouse.y >= entry.y
                    && entry.x + entry.width > mouse.x && entry.y + entry.height > mouse.y) {
                this.hoveredEntry = entry;
                hit = entry;
            }
        }
        for (ItemListEntry entry : this.entries) {
            entry.draw(mouse, (com.adjustclient.event.Render2DEvent) context, entry == this.hoveredEntry);
        }
    }

    public ItemStack getStack(int id) {
        Item item = Item.getItemById(id);
        return new ItemStack(item, 1, 0);
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (this.search != null) {
            if (!pressed) {
                return false;
            }
            boolean focus = false;
            if (button == 0 && this.hitSearch(mouse)) {
                focus = !ClickGuiScreen.instance().searchBar.active;
            }
            this.search.focused = focus;
            if (focus) {
                return true;
            }
        }
        if (!pressed || this.hoveredEntry == null) {
            return false;
        }
        if (this.hoveredEntry instanceof ItemListButton) {
            if (button == 0) {
                this.toggleBrowse();
                ((ItemListButton) this.hoveredEntry).setLabel(this.browsing ? "-" : "+");
            }
            return true;
        }
        if (button == 0) {
            ItemCell cell = ((ItemChooserGrid) this.hoveredEntry).getHoveredCell();
            if (cell != null) {
                int id = cell.id;
                if (this.hotbarTarget != null) {
                    this.hotbarTarget.accept(id);
                } else if (this.browsing) {
                    this.setting.add(id);
                } else {
                    this.setting.remove(id);
                }
                this.grid.refresh();
            }
        }
        return true;
    }

    boolean matches(int id) {
        if (this.searchText.length() == 0) {
            return true;
        }
        String name;
        String display;
        if (this.setting instanceof com.adjustclient.setting.BlocksSetting) {
            net.minecraft.block.Block block = net.minecraft.block.Block.getBlockById(id);
            display = block.getLocalizedName();
            Object key = net.minecraft.block.Block.blockRegistry.getNameForObject(block);
            name = key == null ? "null" : key.toString();
        } else {
            ItemStack stack = this.getStack(id);
            display = stack.getDisplayName();
            Object key = Item.itemRegistry.getNameForObject(stack.getItem());
            name = key == null ? "null" : key.toString();
        }
        String stripped = StringUtils.stripControlCodes(display);
        if (stripped != null && stripped.toLowerCase(Locale.ROOT).indexOf(this.searchText) >= 0) {
            return true;
        }
        if (name.toLowerCase(Locale.ROOT).contains(this.searchText)) {
            return true;
        }
        return Integer.toString(id).contains(this.searchText);
    }

    /** Is the mouse inside the search row (x in [bounds.x, bounds.x + bounds.width], y in [y, y + inputHeight])? */
    boolean hitSearch(Vec2f mouse) {
        float height = this.search == null ? 0.0F
                : (this.search.lineCount() >= 2 ? (float) (this.search.lineCount() - 1) * 7.5F + 9.5F : 9.5F);
        float x = this.bounds.x;
        return mouse.x >= x && x + this.bounds.width > mouse.x && mouse.y >= this.y && this.y + height > mouse.y;
    }

    @Override
    public void onClickOutside(Vec2f mouse, int button, boolean pressed) {
        if (this.search != null && pressed) {
            this.search.focused = false;
        }
    }

    public int getAccentColor() {
        return this.browsing ? 0xFF00FF00 : 0xFFFF0000; // green while browsing the available entries, red for the added ones
    }

    @Override
    public float getHeight() {
        if (this.search == null) {
            return 52.0F;
        }
        int lines = this.search.lineCount();
        return lines < 2 ? 61.5F : (float) (lines - 1) * 7.5F + 9.5F + 52.0F;
    }

    @Override
    public void onUpdate() {
        if (this.search != null) {
            this.search.field.updateCursorCounter();
        }
    }
}
