package com.adjustclient.ui.clickgui;

import com.adjustclient.Adjust;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.TextInputBox;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.module.Category;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.ui.clickgui.frame.TabHeader;
import com.adjustclient.ui.clickgui.setting.BedDefenseSettingComponent;
import com.adjustclient.ui.clickgui.setting.ExpandSettingComponent;
import com.adjustclient.ui.clickgui.setting.color.ColorPicker;
import com.adjustclient.ui.clickgui.setting.color.ColorSettingComponent;
import com.adjustclient.ui.clickgui.setting.group.CollapsibleHeaderComponent;
import com.adjustclient.ui.clickgui.setting.hotbar.HotbarSettingComponent;
import com.adjustclient.ui.clickgui.setting.items.ItemListComponent;
import com.adjustclient.ui.clickgui.setting.preview.EspPreviewComponent;
import com.adjustclient.ui.clickgui.setting.string.StringEditorComponent;
import com.adjustclient.ui.clickgui.setting.string.StringSettingComponent;
import com.adjustclient.ui.dialog.AdjustButton;
import com.adjustclient.ui.screen.AdjustComponent;
import com.adjustclient.ui.screen.AdjustScreen;
import com.adjustclient.ui.screen.transition.DimTransition;
import com.adjustclient.ui.screen.transition.PopupTransition;
import com.adjustclient.util.Vec2f;

public final class ClickGuiScreen extends AdjustScreen {
    public static ClickGuiScreen INSTANCE;
    public boolean layoutDirty;
    public boolean initialized;
    public boolean dragMoved;
    public final ArrayList<ClickGuiTab> tabs;
    private final DimTransition backdrop;
    public final PopupTransition fade;
    public ClickGuiTab draggedTab;
    public ClickGuiTab focusedTab;
    private Vec2f spawnPos;
    public final HashMap<Object, ClickGuiComponent> componentMap;
    final ArrayList<Modifier> overlays;
    private final Animation dragAnimation;
    private final Timer lastActivity;
    public final Timer timer48;
    public final SearchBar searchBar;
    final AdjustButton homeButton;
    public float grabX;
    public float grabY;
    private int frame;
    private int counter;
    private Object obf76;

    public ClickGuiScreen() {
        this.blocksGameInteraction = true;
        this.tabs = new ArrayList<>();
        this.backdrop = new DimTransition(new Animation(Easing.LINEAR, 100L));
        this.fade = new PopupTransition(this, new Animation(Easing.EASE_OUT_EXPO, 220L), 220L);
        this.componentMap = new HashMap<>();
        this.overlays = new ArrayList<>();
        this.counter = 0;
        this.dragAnimation = new Animation(Easing.LINEAR, 130L);
        this.lastActivity = new Timer();
        this.timer48 = new Timer();
        this.searchBar = new SearchBar(this);
        this.homeButton = new AdjustButton(0, "k", 9, 9, 26, ClickGuiScreen::multiplyAlpha75, Fonts.ICONS_13);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.spawnPos = new Vec2f((float) ((double) sr.getScaledWidth() * 0.5) - 350.0F, 84.0F);
        Category[] categories = Category.values();
        for (int i = 0; i < 5; i++) {
            Category category = categories[i];
            Vec2f at = this.spawnPos;
            this.spawnPos = new Vec2f(at.x + 68.0F, at.y);    // 66 px wide tabs with a 2 px gap
            CategoryTab tab = new CategoryTab(at, this, category);
            tab.init();
            tabs.add(tab);
        }
        Vec2f at = this.spawnPos;
        this.spawnPos = new Vec2f(at.x + 68.0F, at.y);
        ConfigurationTab configs = new ConfigurationTab(this, at);
        tabs.add(configs);
        this.layoutDirty = true;
        UserConfiguration.secondaryScale.addListener(() -> this.layoutDirty = true);
        tabs.forEach(ClickGuiScreen::assignOwner);
    }

    public static ClickGuiScreen instance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiScreen();
        }
        return INSTANCE;
    }

    private static void assignOwner(ClickGuiTab tab) {
        ArrayList<ClickGuiComponent> entries = tab.getEntries();
        if (entries == null) {
            return;
        }
        for (ClickGuiComponent e : entries) {
            e.owner = tab;
        }
    }

    void refreshTab(ClickGuiTab tab) {
        tab.renderList.clear();
        tab.rowList.clear();
        tab.layoutList.clear();
        boolean searching = searchBar.isSearching();
        ClickGuiComponent state = tab.getState();
        if (searching && state != null && !state.expanded) {
            state.setExpanded(true);
            state.refreshPending = true;
        }
        ArrayList<ClickGuiComponent> entries = tab.getEntries();
        if (entries != null) {
            for (ClickGuiComponent e : new ArrayList<>(entries)) {
                if (e.isHeader()) {
                    tab.rowList.add(e);
                    if (!ClickGuiComponent.isFullyExpanded(e)) {
                        break;
                    }
                    continue;
                }
                if (e.parent != null && !ClickGuiComponent.isFullyExpanded(e.parent)) {
                    continue;
                }
                if (!e.shown) {
                    continue;
                }
                if (searching && !searchBar.matches(e)) {
                    continue;
                }
                tab.renderList.add(e);
            }
        }
        tab.layoutList.addAll(tab.rowList);
        tab.layoutList.addAll(tab.renderList);
    }

    /** Adds a screen overlay; exclusive overlays (canStack() == false, e.g. the bind overlay) are only added when no overlay exists. */
    public void addOverlay(Modifier overlay) {
        if (overlay.canStack() || overlays.isEmpty()) {
            overlays.add(overlay);
        }
    }

    public void removeOverlay(Modifier overlay) {
        overlays.remove(overlay);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        return keyTyped((int) typedChar, keyCode);
    }

    @Override
    public void mouseScrolled(Vec2f mouse, int button, boolean pressed, int wheel, float direction) {
        onScroll(mouse, direction);
    }

    @Override
    public void onAdded() {
        markDirty();
    }

    /** (typedChar, keyCode): overlays first, then the components of all tabs, then the search bar, finally ESC closes the GUI. */
    public boolean keyTyped(int typedChar, int keyCode) {
        for (Modifier o : overlays.toArray(new Modifier[0])) {
            if (o.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        for (ClickGuiTab tab : tabs) {
            ArrayList<ClickGuiComponent> list = tab.layoutList;
            for (ClickGuiComponent c : list) {
                if (c.keyTyped(typedChar, keyCode)) {
                    return true;
                }
            }
        }
        if (searchBar.keyTyped(typedChar, keyCode)) {
            return true;
        }
        if (keyCode != Keyboard.KEY_ESCAPE) {
            return false;
        }
        fade.close();
        return true;
    }

    public void onScroll(Vec2f mouse, float direction) {
        for (Modifier o : overlays.toArray(new Modifier[0])) {
            if (o.onScroll(direction)) {
                return;
            }
        }
        for (ClickGuiTab tab : tabs) {
            for (ClickGuiComponent c : tab.layoutList) {
                if (mouse.x < c.x || mouse.y < c.y || c.x + c.width <= mouse.x || c.y + c.height <= mouse.y) {
                    continue;
                }
                if (c.onScroll(mouse, direction)) {
                    return;
                }
            }
        }
        for (ClickGuiTab tab : tabs) {
            float tx = tab.getX();
            float ty = tab.getY();
            if (mouse.x < tx || mouse.y < ty || tx + tab.width <= mouse.x || ty + tab.height <= mouse.y) {
                continue;
            }
            tab.scrollBy(-direction * 14.0F);
            timer48.reset();
            layoutDirty = true;
        }
    }

    /** Places the home button (base grey 45) left of the search field and draws it. */
    void drawHomeButton(Vec2f mouse, ScaledResolution window) {
        float w = (float) window.getScaledWidth();
        AdjustButton b = this.homeButton;
        b.base = 45;
        float fieldWidth = searchBar.textInput.width;
        float bw = (float) b.width;
        b.x = ((w - fieldWidth) - bw) * 0.5F - bw - 2.0F;
        b.y = 13.0F;
        b.draw(mouse.x, mouse.y);
    }

    public void render(Object queue, Vec2f mouse, ScaledResolution window) {
        renderScreen(queue, mouse, window);
        // net.minecraft GlStateManager alphaState.setDisabled() + GL11.glDisable(GL_ALPHA_TEST = 3008)
        GlStateManager.disableAlpha();
    }

    /** (mouse, button, pressed); button -1 = mouse move. */
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (button == 0 && !pressed) {
            this.draggedTab = null;
        }
        // search text field (158 px wide, y 13): a left press inside focuses it, every other press unfocuses it
        TextInputBox tf = searchBar.textInput;
        boolean insideField = false;
        if (pressed) {
            if (button == 0 && mouse.x >= tf.x && mouse.y >= tf.y && tf.x + tf.width > mouse.x && tf.y + tf.height > mouse.y) {
                tf.input.focused = true;
                insideField = true;
            } else {
                tf.input.focused = false;
            }
        }
        // search field focus loss / gain toggles the search
        if (tf.input.focused != searchBar.active) {
            if (tf.input.focused || !searchBar.isSearching()) {
                searchBar.toggle();
                return true;
            }
            if (insideField) {
                return true;
            }
        } else if (insideField) {
            return true;
        }
        // home button: only a left button press opens the Adjust home screen
        AdjustButton hb = this.homeButton;
        if (pressed && button == 0 && hb.isHovered(mouse.x, mouse.y)) {
            Adjust.INSTANCE.screenStack.openFriendsScreen();
            return true;
        }
        if (button != -1) {
            for (Modifier o : overlays.toArray(new Modifier[0])) {
                if (o.mouseClicked(mouse, button, pressed)) {
                    return true;
                }
            }
        }
        for (ClickGuiTab tab : new ArrayList<>(tabs)) {                     // list order: the last tab is drawn in front
            float bottom = tab.height + tab.getY();
            for (ClickGuiComponent c : new ArrayList<>(tab.layoutList)) {
                if (!tab.hovered) {
                    c.onClickOutside(mouse, button, pressed);
                    continue;
                }
                if (mouse.y >= bottom) {
                    break;                                                  // rows below the tab body get no events
                }
                boolean inside = mouse.x >= c.x && mouse.y >= c.y && c.x + c.width > mouse.x && c.y + c.height > mouse.y;
                if (inside) {
                    if (c.mouseClicked(mouse, button, pressed)) {
                        return true;
                    }
                } else {
                    c.onClickOutside(mouse, button, pressed);
                }
            }
        }
        // the tab drag (draggedTab / grabX / grabY) is started by the tab header row (TabHeader.mouseClicked), not here
        return false;
    }

    public void markDirty() {
        this.layoutDirty = true;
    }

    public void updateScreen() {
        for (ClickGuiTab tab : tabs) {
            for (ClickGuiComponent c : tab.layoutList) {
                c.onUpdate();
            }
        }
        counter++;
        lastActivity.reset();
        searchBar.textInput.input.field.updateCursorCounter();
    }

    public void onOpen() {
        backdrop.animation.value = 0.0F;
        backdrop.animation.restart();
        counter = 0;
        if (lastActivity.lastMs + 60000L <= System.currentTimeMillis()) {
            fade.animation.duration = 400L;                                 // opened again after > 60 s: slower fade-in
            lastActivity.reset();
        } else {
            fade.animation.duration = 220L;
        }
    }

    public void onClose() {
        for (Modifier m : allModifiers()) {
            m.reset();
        }
        closeAll();                                                         // c.o
        this.draggedTab = null;
        this.layoutDirty = true;
        backdrop.animation.value = 0.0F;
        backdrop.animation.restart();
        overlays.clear();
    }

    /**
     * Lays out the rows of one tab top to bottom starting at the tab's y: each row gets width = tab width, x = tab.x, y = running y
     * minus the animated scroll offset (header rows are not scrolled), height unchanged; the background colour of the row depends on the
     * nesting depth d (number of parents, 0 for rows without parent): ARGB(170, 19 - 3 * min(d, 3), same, same); the inner bounds are
     * inset by d px on every side.
     */
    void layoutTab(ClickGuiTab tab) {
        float y = tab.getY();                                                // y after clamping (anchor.y when present)
        float x = tab.getX();                                                // x after clamping (anchor.x when present)
        float width = tab.width;
        float scroll = tab.scrollAnimation.value;
        float running = y;
        for (ClickGuiComponent c : tab.layoutList) {
            c.width = width;
            float scrolled = c.isHeader() ? 0.0F : scroll;
            c.x = x;
            c.y = running - scrolled;
            float next = running + c.height;
            int depth = 0;
            if (!noInsetRowType(c)) {
                for (ClickGuiComponent p = c.parent; p != null; p = p.parent) {
                    depth++;
                }
            }
            int shade = 19 - 3 * Math.min(depth, 3);                         // 19, 16, 13, 10
            c.backgroundColor = 0xAA000000 | (shade << 16) | (shade << 8) | shade;
            float dx = c.getX() + (float) depth;
            float dy = c.getY();
            c.bounds.x = dx;
            c.bounds.y = dy;
            float bw = (c.width - (float) depth) - (float) depth;
            if (bw != c.bounds.width && c instanceof StringEditorComponent) {
                ((StringEditorComponent) c).onWidthChanged();
            }
            c.bounds.width = bw;
            c.bounds.height = c.height;
            running = next;
        }
    }

    void renderScreen(Object queue, Vec2f mouse, ScaledResolution window) {
        if (fade.getProgress()) {
            return;
        }
        dragAnimation.update(draggedTab != null ? 1.0F : 0.0F);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        if (!initialized) {
            initialized = true;
            float total = 0.0F;
            for (ClickGuiTab t : tabs) {
                total += 2.0F + t.width;
            }
            float x = ((float) window.getScaledWidth() / 2.0F) - (total / 2.0F);  // tabs centred, 2 px gaps
            for (ClickGuiTab t : tabs) {
                t.x = x;
                t.y = (float) window.getScaledHeight() / 4.5F;               // tab top at 1/4.5 of the window height
                x += 2.0F + t.width;
            }
        }
        if (draggedTab != null) {
            if (tabs.get(tabs.size() - 1) != draggedTab) {                   // bring the dragged tab to the front (end of the list = drawn last)
                tabs.remove(draggedTab);
                tabs.add(draggedTab);
            }
            draggedTab.x = mouse.x - grabX;
            draggedTab.y = mouse.y - grabY;
            dragMoved = true;
            layoutDirty = true;
        }
        updateShownFlags();                                                  // c.y
        for (ClickGuiTab tab : tabs) {
            if (counter == 0) {
                tab.scrollAnimation.value = tab.scroll;
                tab.scrollAnimation.restart();
            }
            tab.scrollAnimation.update(tab.scroll);                          // 135 ms follow animation
            if (!tab.scrollAnimation.finished) {
                layoutDirty = true;
            }
        }
        if (layoutDirty) {
            refreshAllTabs();                                                // c.w
            layoutAllTabs();                                                 // c.x
            layoutDirty = false;
        }
        ClickGuiTab hovered = null;                                          // topmost tab under the mouse (list is back to front)
        for (int i = tabs.size() - 1; i >= 0; i--) {
            if (tabs.get(i).contains(mouse)) {
                hovered = tabs.get(i);
                break;
            }
        }
        float previousAlpha = NvgDraw.getGlobalAlpha();
        NvgDraw.setGlobalAlpha(fade.animation.value);
        backdrop.draw(window, draggedTab != null ? 95.0F : 45.0F);
        NvgDraw.setGlobalAlpha(previousAlpha);
        Render2DEvent top = new Render2DEvent(window, 0.0F);                 // frame level queue: drawn after all tabs (tooltips, popups)
        for (ClickGuiTab tab : new ArrayList<>(tabs)) {
            NvgDraw.setGlobalAlpha(tabAlpha(tab));
            tab.hovered = tab == hovered;
            float tabX = tab.getX();
            float tabY = tab.getY();
            float bottom = tab.height + tabY;
            boolean mouseAboveBottom = tab == hovered && bottom > mouse.y;
            Render2DEvent rowQueue = new Render2DEvent(window, 0.0F);        // per-tab queue: drawn inside the tab clip after the rows
            boolean headerHovered = false;
            for (ClickGuiComponent c : tab.rowList) {
                if (c.getY() >= bottom) {
                    break;
                }
                c.hovered = mouseAboveBottom && containsPoint(c.bounds, mouse);
                headerHovered |= c.hovered;
                c.render(rowQueue, top, mouse, window);
            }
            // clip rectangle of the body: x = tab.x - 0.5, y = tab.y + 10.5 (below the header), width = tab.width + 1.0, height = tab.height - 10.5
            RenderUtil.pushScissor(window, 1, tabX - 0.5F, tabY + 10.5F, 1.0F + tab.width, tab.height - 10.5F);
            for (ClickGuiComponent c : tab.renderList) {
                if (c.getY() >= bottom) {
                    break;
                }
                float rowTop = c.getY();
                float rowBottom = rowTop + c.height;
                NvgDraw.fillRect(c.backgroundColor, c.bounds.x, rowTop, c.bounds.width + c.bounds.x, rowBottom);
                if (separatorRowType(c) && ClickGuiComponent.isFullyExpanded(c)) {
                    NvgContext.pushEvenScale();
                    Vector4f r = NvgContext.toPixels(c.bounds.x, c.y + c.height - 0.5F, c.bounds.x + c.bounds.width, c.y + c.height);
                    NvgDraw.fillRect(0xAA373738, r.x, r.y, r.z, r.w);
                    NvgContext.popEvenScale();
                }
            }
            for (ClickGuiComponent c : tab.renderList) {
                if (c.getY() >= bottom) {
                    break;
                }
                c.hovered = !headerHovered && mouseAboveBottom && containsPoint(c.bounds, mouse);
                c.render(rowQueue, top, mouse, window);
            }
            if (!rowQueue.postTasks.isEmpty()) {
                NvgContext.flushFrame();
                RenderUtil.pushScissor(window, 1, tabX, tabY + 11.0F, tab.width, tab.height - 11.0F);
                rowQueue.runPostTasks();
                RenderUtil.popScissor();
            }
            RenderUtil.popScissor();
        }
        top.runPostTasks();
        NvgDraw.setGlobalAlpha(previousAlpha);
        // overlays (bind overlay, dim ...)
        NvgDraw.setGlobalAlpha(fade.animation.value);
        for (Modifier o : new ArrayList<>(overlays)) {
            o.render(window, (Render2DEvent) queue, (Render2DEvent) queue, mouse, false);
        }
        searchBar.render(mouse, window);
        drawHomeButton(mouse, window);
        GlStateManager.enableDepth();                                    // restore GL_DEPTH_TEST
        NvgDraw.setGlobalAlpha(previousAlpha);
    }

    private static boolean containsPoint(AdjustComponent r, Vec2f p) {
        return p.x >= r.x && p.y >= r.y && r.x + r.width > p.x && r.y + r.height > p.y;
    }

    private static boolean noInsetRowType(ClickGuiComponent c) {
        return c instanceof ColorPicker || c instanceof HotbarSettingComponent
                || c instanceof BedDefenseSettingComponent || c instanceof EspPreviewComponent
                || c instanceof ItemListComponent;
    }

    private static boolean separatorRowType(ClickGuiComponent c) {
        return c instanceof ExpandSettingComponent || c instanceof StringSettingComponent
                || c instanceof CollapsibleHeaderComponent || c instanceof ColorSettingComponent;
    }

    /** Alpha used for one tab: 1.0F for the focused tab, 1 - 0.65 * dragAnimation for the others, never above the fade value. */
    float tabAlpha(ClickGuiTab tab) {
        float a = 1.0F;
        if (focusedTab != tab) {
            a = 1.0F - 0.65F * dragAnimation.value;                         // 0.64999998F
        }
        return Math.min(a, fade.animation.value);
    }

    static int multiplyAlpha75(int alpha) {
        return (int) ((float) alpha * 0.75F);
    }

    static final IntFunction<Modifier[]> NEW_MODIFIER_ARRAY = Modifier[]::new;

    static Consumer<ClickGuiComponent> ownerSetter(ClickGuiTab tab) {
        return c -> c.owner = tab;
    }

    /** For every entry of every tab: collapse rows that were only expanded temporarily (setExpanded(false)), then call onScreenClosed(). */
    void closeAll() {
        for (ClickGuiTab tab : tabs) {
            for (ClickGuiComponent e : tab.getEntries()) {
                if (e.refreshPending) {
                    e.setExpanded(false);
                }
                e.onScreenClosed();
            }
        }
    }

    /** Collapses the temporarily expanded rows of every tab (after the search text changed). */
    void collapseTemporaryRows() {
        for (ClickGuiTab tab : tabs) {
            for (ClickGuiComponent e : tab.getEntries()) {
                if (e.refreshPending) {
                    e.setExpanded(false);
                }
            }
        }
    }

    /** Resets the state of every modifier (same dispatch as {@link #onClose}'s loop). */
    public void resetModifiers() {
        for (Modifier m : allModifiers()) {
            m.reset();
        }
    }

    void refreshAllTabs() {
        for (ClickGuiTab tab : tabs) {
            refreshTab(tab);
        }
    }

    void layoutAllTabs() {
        ScaledResolution resolution = screenResolution != null
                ? screenResolution
                : new ScaledResolution(Minecraft.getMinecraft(), com.adjustclient.module.impl.render.hud.HUDModule.getClickGuiScale());
        for (ClickGuiTab tab : tabs) {
            tab.anchor = null;
            float windowHeight = (float) resolution.getScaledHeight();
            float running = tab.y;
            for (ClickGuiComponent c : tab.layoutList) {
                float h = c.measureHeight();
                c.height = h;
                running += h;
            }
            tab.contentHeight = running - tab.getY();
            float maxHeight = windowHeight / 1.75F;
            tab.height = Math.min(tab.contentHeight, maxHeight);
            float over = windowHeight - (tab.getY() + tab.height);           // free space below the tab
            if (over < 5.0F) {
                tab.height = tab.height - Math.abs(over);
                if (tab.height < 11.0F) {
                    tab.height = 11.0F;
                }
            }
            tab.scroll = Math.max(0.0F, Math.min(tab.scroll, tab.contentHeight - tab.height));
            tab.anchor = new Vec2f(tab.clampX(), tab.clampY());
            layoutTab(tab);
        }
    }

    void updateShownFlags() {
        frame++;
        for (ClickGuiTab tab : tabs) {
            for (ClickGuiComponent c : tab.getEntries()) {
                boolean visible = c.computeVisible(frame);
                if (c.shown != visible) {
                    c.shown = visible;
                    layoutDirty = true;
                }
            }
        }
    }

    /** All modifiers of all entries of all tabs (flat map over tab.getEntries() and entry.modifiers); ConfigurationTab entries refresh when stale. */
    ArrayList<Modifier> allModifiers() {
        ArrayList<Modifier> out = new ArrayList<>(128);
        for (ClickGuiTab tab : tabs) {
            ArrayList<ClickGuiComponent> entries = tab.getEntries();
            for (ClickGuiComponent c : entries) {
                out.addAll(c.modifiers);
            }
        }
        return out;
    }
}
