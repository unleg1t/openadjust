package com.adjustclient.ui.clickgui.setting.hotbar;

import com.adjustclient.setting.HotbarSetting;
import com.adjustclient.setting.ItemsSetting;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.ui.clickgui.setting.items.ItemChooserGrid;
import com.adjustclient.util.Vec2f;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.screen.AdjustComponent;
import net.minecraft.init.Items;

public final class HotbarSettingComponent extends SettingComponent<HotbarSetting> {

    public static final BiMap<Item, Integer> POTIONS = HashBiMap.create(16);

    private HotbarSlot[] cells;
    private HotbarItemChooser chooser;
    private HotbarSlot selected;
    private IntArrayList filteredIds;
    private IntArrayList allIds;

    static {
        POTIONS.put(Items.diamond, 16451);
        POTIONS.put(Items.saddle, 16385);
        POTIONS.put(Items.dye, 16418);
        POTIONS.put(Items.map, 16457);
        POTIONS.put(Items.string, 16421);
    }

    public HotbarSettingComponent(HotbarSetting setting) {
        super(setting);
        this.cells = new HotbarSlot[10];
        this.filteredIds = new IntArrayList();
        this.allIds = new IntArrayList();
        this.modifiers.clear();
        for (int i = 0; i < 9; i++) {
            this.cells[i] = new HotbarSlot(this, i);
        }
        this.cells[9] = new HotbarClearSlot(this);
        for (ItemStack stack : HotbarSetting.PALETTE.values()) { // HotbarSetting's item table (LinkedHashMap<?, ItemStack>)
            this.allIds.add(Item.getIdFromItem(stack.getItem()));
        }
        ItemsSetting hotbarItems = new ItemsSetting(null, "hotbar items", this.filteredIds);
        this.chooser = new HotbarItemChooser(hotbarItems);
        Consumer<Integer> onPick = this::pick;
        this.chooser.hotbarTarget = onPick;
        this.setting.addListener(this::syncFromSetting);
        this.syncFromSetting();
    }

    void commit() {
        ItemStack[] value = this.setting.getValue();
        for (int i = 0; i < value.length; i++) {
            ItemStack stack = this.cells[i].stack;
            if (stack == null || stack.getItem() == null) {
                value[i] = null;
                continue;
            }
            Item item = stack.getItem();
            if (item instanceof ItemPotion) {
                Item mapped = POTIONS.inverse().get(stack.getMetadata());
                if (mapped == null) {
                    value[i] = null;
                    continue;
                }
                item = mapped;
            }
            value[i] = new ItemStack(item, 1, 0);
        }
        this.setting.notifyListeners(value, value);
    }

    void syncFromSetting() {
        ItemStack[] value = this.setting.getValue();
        for (int i = 0; i < value.length; i++) {
            ItemStack stack = value[i];
            if (stack == null) {
                this.cells[i].stack = null;
                continue;
            }
            Integer meta = POTIONS.get(stack.getItem());
            this.cells[i].stack = meta == null
                    ? new ItemStack(stack.getItem(), 1, 0)
                    : new ItemStack(net.minecraft.init.Items.potionitem, 1, Math.max(meta, 0));
        }
    }

    /** Sorts the filtered ids by their position in the all-ids list (IntArrayList.sort = IntArrays.mergeSort with the comparator below). */
    void sortFiltered() {
        it.unimi.dsi.fastutil.ints.IntArrays.mergeSort(this.filteredIds.elements(), 0, this.filteredIds.size(),
                this::comparePositions);
    }

    /** Comparator body of {@link #sortFiltered()}: order of the two ids in the all-ids list. */
    private int comparePositions(int a, int b) {
        return Integer.compare(this.allIds.indexOf(a), this.allIds.indexOf(b));
    }

    /**
     * Select a slot ({@code cell != null}) or deselect ({@code null}). Selecting the clear cell empties every slot instead and commits.
     * Deselecting rebuilds {@link #filteredIds} (all ids that are not yet used, see {@link #isUsed(int)}), refreshes the chooser grid and,
     * when the row height changed (29 -&gt; chooser height), asks the GUI root for a relayout.
     */
    void select(HotbarSlot cell) {
        if (cell instanceof HotbarClearSlot) {
            for (HotbarSlot slot : this.cells) {
                slot.stack = null;
            }
            this.commit();
            return;
        }
        if (cell != null) {
            this.filteredIds.clear();
            for (int i = 0; i < this.allIds.size(); i++) {
                int id = this.allIds.getInt(i);
                if (!this.isUsed(id)) {
                    this.filteredIds.add(id);
                }
            }
            this.chooser.getSetting().notifyListeners(this.filteredIds, this.filteredIds);
            this.sortFiltered();
            this.selected = cell;
            if (this.filteredIds.isEmpty()) {
                return;
            }
        } else {
            this.selected = null;
        }
        if (this.getHeight() != this.height) {
            com.adjustclient.ui.clickgui.ClickGuiScreen.instance().markDirty(); // relayout wanted
        }
    }

    public void pick(int id) {
        ItemStack stack = this.chooser.getStack(id);
        this.selected.stack = stack;
        this.select(null);
        this.commit();
    }

    /**
     * Is the item with this list position already in one of the slots? A slot matches when its stack's item is the item, or (potion
     * stacks) when {@link #POTIONS}{@code .inverse()} maps the stack's damage value to the item. Empty slots never match.
     * Gap-fill 2026-11-05: rewritten from the stream form; the previous version dropped the non-potion branch (item identity).
     */
    boolean isUsed(int id) {
        Item item = Item.getItemById(id);
        return java.util.Arrays.stream(this.cells).anyMatch(slot -> {
            if (slot.stack == null) {
                return false;
            }
            Item slotItem = slot.stack.getItem();
            if (slotItem instanceof ItemPotion) {
                slotItem = POTIONS.inverse().get(slot.stack.getMetadata());
            }
            return slotItem == item;
        });
    }

    @Override
    public void onScreenClosed() {
        this.selected = null;
        if (this.height != 29.0F) {
            com.adjustclient.ui.clickgui.ClickGuiScreen.instance().markDirty();
        }
    }

    @Override
    public boolean keyTyped(int typedChar, int keyCode) {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            return false;
        }
        if (this.selected != null) {
            this.select(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(Vec2f mouse, float delta) {
        if (this.selected == null) {
            return false;
        }
        ItemChooserGrid grid = this.chooser.getGrid();
        if (mouse.x < grid.x || mouse.y < grid.y || grid.x + grid.width <= mouse.x || grid.y + grid.height <= mouse.y) {
            return false;
        }
        return grid.scrollBy(delta); // 13 per step, clamp 0 .. (rows - 4) * 13
    }

    /**
     * With a slot selected the chooser is placed on this row's rectangle and rendered. Otherwise the cells are laid out in a wrapping row:
     * x starts at row.x + 2.5, y at row.y + 2.0, cells are 11 x 11, advance = 11 + 1.5; when the next cell would not fit
     * (x + w + 1.5 >= row.width - cell.width) the row wraps by 11 + 3.0. Hovering a cell shows the hand cursor (221188).
     */
    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent context, Vec2f mouse) {
        float x = this.x;
        float y = this.y;
        if (this.selected != null) {
            this.chooser.x = x;
            this.chooser.y = y;
            this.chooser.width = this.width;
            this.chooser.height = this.height;
            this.chooser.renderContent(context, mouse);
            return;
        }
        float cx = 0.0F;
        float cy = y + 2.0F;
        for (HotbarSlot cell : this.cells) {
            cell.x = x + 2.5F + cx;
            cell.y = cy;
            cell.width = 11.0F;
            cell.height = 11.0F;
            AdjustComponent scissor = NvgDraw.peekScissor();
            boolean over = scissor == null
                    ? mouse.x >= cell.x && mouse.y >= cell.y && cell.x + cell.width > mouse.x && cell.y + cell.height > mouse.y
                    : cell.intersects(scissor, mouse.x, mouse.y);
            cell.hovered = over;
            if (over) {
                RenderUtil.cursor = 221188; // hand cursor
            }
            cx += cell.width + 1.5F;
            cell.drawBackground((com.adjustclient.event.Render2DEvent) context, mouse);
            if (cx >= this.width - cell.width) {
                cx = 0.0F;
                cy += cell.height + 3.0F;
            }
        }
    }

    /**
     * With a slot selected the click is handled like an {@link ItemListComponent} (search focus, +/- button, grid cell: the cell's id goes to
     * {@link #pick(int)}). Otherwise on press: the cell under the mouse is selected (left button) or emptied (button 1 and 2, then commit).
     */
    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (this.selected != null) {
            return this.chooser.onMousePressed(mouse, button, pressed);
        }
        if (!pressed) {
            return false;
        }
        for (HotbarSlot cell : this.cells) {
            if (mouse.x >= cell.x && mouse.y >= cell.y && cell.x + cell.width > mouse.x && cell.y + cell.height > mouse.y) {
                if (button != 0) {
                    if (button < 3) {
                        cell.stack = null;
                        this.commit();
                    }
                    return true;
                }
                this.select(cell);
                return true;
            }
        }
        return false;
    }

    /** 29.0 while no slot is selected, otherwise the chooser's height (52, 61.5 or 61.5 + 7.5 per extra search line). */
    @Override
    public float getHeight() {
        if (this.selected == null) {
            return 29.0F;
        }
        if (this.chooser.getSearch() == null) {
            return 52.0F;
        }
        int lines = this.chooser.getSearch().lineCount();
        return lines < 2 ? 61.5F : (float) (lines - 1) * 7.5F + 9.5F + 52.0F;
    }
}
