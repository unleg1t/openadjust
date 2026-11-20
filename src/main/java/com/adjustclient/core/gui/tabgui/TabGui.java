package com.adjustclient.core.gui.tabgui;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.settings.BlocksSetting;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ButtonSetting;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.HotbarSetting;
import com.adjustclient.core.settings.ItemsSetting;
import com.adjustclient.core.settings.KeybindSetting;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.StringSetting;
import com.adjustclient.core.gui.hud.HudElement;
import com.adjustclient.core.gui.hud.layout.Rect2f;
import com.adjustclient.core.gui.hud.tabgui.TabGuiSettings;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.movable.SnapGuide;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefenseSetting;
import com.adjustclient.modules.impl.render.esp.EspPreviewSetting;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.GradientRectCommand;
import com.adjustclient.core.gui.tabgui.component.BooleanComponent;
import com.adjustclient.core.gui.tabgui.component.ButtonComponent;
import com.adjustclient.core.gui.tabgui.component.CategoryComponent;
import com.adjustclient.core.gui.tabgui.component.ColorComponent;
import com.adjustclient.core.gui.tabgui.component.ExpandComponent;
import com.adjustclient.core.gui.tabgui.component.KeybindAddComponent;
import com.adjustclient.core.gui.tabgui.component.KeybindKeyComponent;
import com.adjustclient.core.gui.tabgui.component.ModeComponent;
import com.adjustclient.core.gui.tabgui.component.ModeOptionComponent;
import com.adjustclient.core.gui.tabgui.component.ModuleComponent;
import com.adjustclient.core.gui.tabgui.component.NumberComponent;
import com.adjustclient.core.gui.tabgui.component.SliderComponent;
import com.adjustclient.core.gui.tabgui.component.StringListComponent;
import com.adjustclient.core.gui.tabgui.component.StringListEditorComponent;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.ScreenUtil;

public final class TabGui extends HudElement {
    public static boolean layingOutNodes;

    /** The HUD > Tab GUI settings (Enabled / Gradient / Auto Close, column width 49, row height 8). */
    public TabGuiSettings settings;
    private SnapGuide anchorA;
    private SnapGuide anchorB;
    /** Root of the node tree ({@link RootNode}); null until the first use ({@link #buildTree()}). */
    public GuiNode root;
    private NvgRenderer drawList;
    /** Width of each laid-out column: node -> Float (the column that holds the node's children). */
    private HashMap<GuiNode, Float> columnWidths;
    /**
     * Auto-close counter: ticks spent walking while a menu column is open; -1 = already closed by auto close,
     * reset to 0 on every handled key press. Overflowing 200 closes all columns, see {@link #updateAutoClose()}.
     */
    private int idleTicks;
    /** Movement grace counter of the auto close: set to 9 while a movement key is held, counts down below zero. */
    private int moveGrace;

    // Default placement of the menu: top-left corner of the screen (x = 0, y = 0).
    {
        this.movable = com.adjustclient.core.gui.hud.Anchor.TOP_LEFT;
    }

    public TabGui(TabGuiSettings settings) {
        this.settings = settings;
        this.anchorA = Adjust.movableManager != null ? Adjust.movableManager.createGuide() : new SnapGuide();
        this.anchorA.active = true;
        this.anchorA.field17 = false;
        this.anchorB = Adjust.movableManager != null ? Adjust.movableManager.createGuide() : new SnapGuide();
        this.anchorB.active = false;
        this.anchorB.field17 = false;
        this.drawList = new NvgRenderer();
        this.columnWidths = new HashMap<>();
    }

    // ================================================================================================================
    // HudElement overrides
    // ================================================================================================================

    /**
     * Key press while the Tab GUI is shown: lazily builds the tree, lets {@link #handleKey(int)} process the key and
     * resets the auto-close counter when the key was consumed.
     *
     * @return true when the key was consumed
     */
    @Override
    public boolean onKey(int key) {
        if (this.root == null) {
            this.root = this.buildTree();
        }
        boolean handled = this.handleKey(key);
        if (handled) {
            this.idleTicks = 0;
            return true;
        }
        return false;
    }

    /**
     * Frame update: lays the node tree out into the draw list and returns it. Also drives the animations of the
     * focused column (via {@link NodeState#getExpandProgress(boolean)}) and wraps everything that was drawn into a
     * clip rectangle whose right edge follows the open animation.
     *
     * @return the filled draw list (the base class renders it)
     */
    @Override
    public NvgRenderer build(ScaledResolution sr) {
        if (this.root == null) {
            this.root = this.buildTree();
        }
        NvgRenderer dl = this.drawList;
        // reset the recorder: commands.clear(); stack.clear(); colour = -1; insertIndex = -1
        dl.commands.clear();
        dl.transforms.clear();
        dl.color = -1;        // current colour
        dl.insertionIndex = -1;        // insert index (-1 = append)
        this.columnWidths.clear();
        TabGuiSettings settings = this.settings;
        // columns are laid out from x = 0 with the minimum column width as the "previous" width
        this.layoutNode(dl, this.root, -1, 0, 0, (float) settings.width, 0.0F);

        // 1) walk down while the submenu animation of the current state is still running (value != 0)
        NodeState animated = this.root.state;
        while (true) {
            Animation anim = animated.expandAnimation;
            if (anim == null) {
                animated.expandAnimation = new Animation(Easing.EASE_OUT_EXPO, 0L);
                break;
            }
            if (0.0F == anim.value) {
                break;
            }
            List<GuiNode> children = animated.node.getChildren();
            if (children.size() == 0) {
                break;
            }
            NodeState next = children.get(animated.getSelectedIndex()).state;
            if (next == animated) {
                break;
            }
            animated = next;
        }
        // 2) the state that really holds the cursor (deepest expanded)
        NodeState focus = this.getFocusedState();

        // 3) advance the open/close animations of the animated state and of its parent
        boolean fast = focus != animated;
        animated.getExpandProgress(fast);
        if (animated.parent == null || animated.parent.state == null) return dl;
        NodeState parentState = animated.parent.state;
        parentState.getExpandProgress(fast);

        Rect2f bounds = dl.getBounds();
        dl.insertionIndex = 0;                                  // insert at the front, i.e. before the text commands
        Float width = this.columnWidths.get(animated.node);
        if (width == null) {
            width = (float) settings.width;
        }
        Animation open = parentState.expandAnimation;
        if (open == null) {
            open = new Animation(Easing.EASE_OUT_EXPO, 0L);
            parentState.expandAnimation = open;
        }
        float x = bounds.x - 0.5F;
        float y = bounds.y - 0.5F;
        float w = (bounds.width - (1.0F - open.value) * width) + 1.0F;
        float h = 0.5F + bounds.height;
        dl.pushClip(x, y, w, h);
        dl.popClip();
        return dl;
    }

    /**
     * Mouse button input: forwarded to the selected row if it is a {@link MouseInputHandler}
     * (the keybind capture rows). {@code (pressed, button)} are the two ints of {@code HudElement.onMouse(int a2, int a3)}.
     *
     * @return the handler's result, false otherwise
     */
    @Override
    public boolean onMouse(int pressed, int button) {
        if (this.root == null) {
            this.root = this.buildTree();
        }
        NodeState focus = this.getFocusedState();
        GuiNode selected = focus.node.getChildren().get(focus.getSelectedIndex());
        if (selected instanceof MouseInputHandler) {
            return ((MouseInputHandler) selected).mouseButton(pressed != 0, button);
        }
        return false;
    }

    @Override
    public void postRender(NvgRenderer dl, ScaledResolution screen, Rect2f bounds) {
        float x = bounds.x + 2.0F;
        float y = (bounds.y + bounds.height) + 0.5F;
        float step = topMargin();
        this.anchorA.x = x;
        y = y + step;
        this.anchorA.y = y;
        this.anchorB.x = (bounds.x + bounds.width) + 3.0F;
        this.anchorB.y = 0.0F;
    }

    /** The Tab GUI is shown while its "Enabled" setting is on and not hidden by its dependency. */
    @Override
    public boolean isEnabled() {
        BooleanSetting enabled = this.settings.enabled;
        return enabled.getAsBoolean();
    }

    @Override
    public void update() {
        if (!this.isEnabled()) {
            return;
        }
        if (this.idleTicks != -1) {
            this.updateAutoClose();
        }
        if (this.root == null) {
            return;
        }
        NodeState focus = this.getFocusedState();
        GuiNode selected = focus.node.getChildren().get(focus.getSelectedIndex());
        if (selected instanceof StringListEditorComponent) {    // text field
            ((StringListEditorComponent) selected).e();
        }
    }

    // ================================================================================================================
    // Input handling
    // ================================================================================================================

    public void forwardKeyEvent(Keyboard.KeyEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (this.root == null) {
            this.root = this.buildTree();
        }
        NodeState focus = this.getFocusedState();
        GuiNode selected = focus.node.getChildren().get(focus.getSelectedIndex());
        if (selected instanceof KeyEventHandler) {
            ((KeyEventHandler) selected).keyEvent(event);
        }
    }

    private boolean handleKey(int key) {
        NodeState focus = this.getFocusedState();
        List<GuiNode> children = focus.node.getChildren();
        GuiNode selected = children.get(focus.getSelectedIndex());
        if (selected instanceof KeyPressHandler) {
            if (((KeyPressHandler) selected).keyPressed(key)) {
                return true;
            }
        }
        // while a keybind entry waits for a key it swallows the navigation keys
        if (selected instanceof KeybindAddComponent && ((KeybindAddComponent) selected).listening /* listening */) {
            return false;
        }
        if (selected instanceof KeybindKeyComponent && ((KeybindKeyComponent) selected).listening /* listening */) {
            return false;
        }
        if (key == Keyboard.KEY_LEFT) {
            GuiNode parent = focus.parent;
            if (parent == null) {
                return false;
            }
            parent.state.expanded = false;
            return true;
        }
        if (key == Keyboard.KEY_RIGHT) {
            boolean wasExpanded = focus.expanded;
            focus.expanded = !selected.getChildren().isEmpty();
            return wasExpanded != focus.expanded;
        }
        if (key == Keyboard.KEY_DOWN) {
            do {
                Animation anim = focus.expandAnimation;
                if (anim == null) {
                    anim = new Animation(Easing.EASE_OUT_EXPO, 0L);
                    focus.expandAnimation = anim;
                }
                anim.value = 0.0F;                       // restart the submenu animation
                focus.selectedIndex = focus.getSelectedIndex() + 1;
                focus.selectedIndex = focus.getSelectedIndex();
                selected = focus.node.getChildren().get(focus.getSelectedIndex());
            } while (selected.isHidden());
            return true;
        }
        if (key == Keyboard.KEY_UP) {
            do {
                Animation anim = focus.expandAnimation;
                if (anim == null) {
                    anim = new Animation(Easing.EASE_OUT_EXPO, 0L);
                    focus.expandAnimation = anim;
                }
                anim.value = 0.0F;
                focus.selectedIndex = focus.getSelectedIndex() - 1;
                focus.selectedIndex = focus.getSelectedIndex();
                selected = focus.node.getChildren().get(focus.getSelectedIndex());
            } while (selected.isHidden());
            return true;
        }
        return false;
    }

    private NodeState getFocusedState() {
        NodeState state = this.root.state;
        while (state.expanded) {
            List<GuiNode> children = state.node.getChildren();
            if (children.size() == 0) {
                break;
            }
            NodeState next = children.get(state.getSelectedIndex()).state;
            if (next == state) {
                break;
            }
            state = next;
        }
        return state;
    }

    // ================================================================================================================
    // Auto close
    // ================================================================================================================

    /**
     * "Auto Close": while the player keeps a movement key held, {@link #idleTicks} grows by one per tick (only
     * during the 8 ticks following a key press, see {@link #moveGrace}); it decays by 3 after more than 9 ticks
     * without movement. When it reaches 201 every open column is closed again (the focus walks back to the root).
     */
    private void updateAutoClose() {
        BooleanSetting autoClose = this.settings.autoClose;
        if (!autoClose.getAsBoolean()) {                         // value && !hideIf
            return;
        }
        Minecraft mc = MinecraftInstance.mc;
        GameSettings gs = mc.gameSettings;
        int idle = this.idleTicks;
        int grace = this.moveGrace;
        if (grace < 1 && (gs.keyBindUseItem.isKeyDown() || gs.keyBindAttack.isKeyDown() || gs.keyBindForward.isKeyDown()
                || gs.keyBindBack.isKeyDown() || gs.keyBindLeft.isKeyDown() || gs.keyBindRight.isKeyDown())) {
            this.moveGrace = 9;
        } else {
            if (grace < -8) {
                this.idleTicks = Math.max(idle - 3, 0);
                this.moveGrace = -1;
                return;
            }
            this.moveGrace = grace - 1;
            if (grace < 2) {
                return;
            }
        }
        idle = idle + 1;
        this.idleTicks = idle;
        if (idle < 201) {
            return;
        }
        this.idleTicks = -1;
        // close every column: repeatedly collapse the parent of the focused state
        while (true) {
            NodeState focus = this.getFocusedState();
            GuiNode parent = focus.parent;
            if (parent == null) {
                break;
            }
            parent.state.expanded = false;
        }
    }

    // ================================================================================================================
    // Layout / drawing
    // ================================================================================================================

    private void layoutNode(NvgRenderer dl, GuiNode node, int depth, int index, int rowBase, float parentWidth, float x) {
        TabGuiSettings settings = this.settings;
        NodeState state = node.state;
        float topMargin = topMargin();
        boolean selected;                               // node is the cursor row of its parent
        boolean open;                                   // its submenu is (being) shown
        if (state.parent == null) {                     // the root
            selected = false;
            open = true;
        } else {
            NodeState parentState = state.parent.state;
            if (parentState.getSelectedIndex() == index) {
                Animation anim = parentState.expandAnimation;
                if (anim == null) {
                    anim = new Animation(Easing.EASE_OUT_EXPO, 0L);
                    parentState.expandAnimation = anim;
                }
                selected = true;
                open = 0.0F != anim.value;
            } else {
                selected = false;
                open = false;
            }
        }

        if (this.root != node) {
            node.render(dl, settings, selected, x + 2.0F, (float) (settings.rowHeight * rowBase) + topMargin, parentWidth);
        }
        if (!open) {
            return;
        }

        // ---- children column -------------------------------------------------------------------------------------
        List<GuiNode> children = node.getChildren();
        float minWidth = (float) settings.width;
        float childX = (parentWidth + x) + 2.5F;       // x of the children column
        float width;
        if (depth < 0) {
            width = minWidth;
            childX = 0.0F;                              // the first column starts at x = 0
            this.columnWidths.put(node, width);
        } else {
            // measure pass
            layingOutNodes = true;
            NvgRenderer scratch = new NvgRenderer();    // ArrayList + Stack, colour -1, insert index -1
            for (GuiNode child : children) {
                if (!child.isHidden()) {
                    child.render(scratch, settings, true, 0.0F, 0.0F, minWidth);
                }
            }
            layingOutNodes = false;
            float measured = scratch.getWidth() + 2.5F;
            if (measured == minWidth) {
                width = minWidth;
            } else {
                width = Math.max(measured + 2.5F, minWidth);
            }
            this.columnWidths.put(node, width);
        }

        // visible row count / selected visible row
        int hidden = 0;
        int selectedRow = 0;                // -1 = no highlight bar (the slider column of a number setting)
        float columnHeight = 0.0F;
        for (int i = 0; i < children.size(); i++) {
            GuiNode child = children.get(i);
            if (child.isHidden()) {
                if (state.getSelectedIndex() == i) {
                    // the selected child vanished (dependency changed): fall back to the first row
                    state.selectedIndex = 0;
                    state.selectedIndex = state.getSelectedIndex();
                    if (state.expanded) {
                        state.expanded = !state.node.getChildren().get(state.getSelectedIndex()).getChildren().isEmpty();
                    } else {
                        state.expanded = false;
                    }
                }
                hidden++;
            } else {
                if (child instanceof SliderComponent) {
                    selectedRow = -1;
                } else if (state.getSelectedIndex() == i) {
                    selectedRow = i - hidden;
                }
                columnHeight = columnHeight + (float) settings.rowHeight;
            }
        }

        // column background rectangle
        int backgroundColor = ((Color) settings.owner).getRGB();
        dl.color(backgroundColor);
        float colTop = (float) (settings.rowHeight * rowBase) + topMargin;
        dl.rect(childX + 2.0F, colTop, width, columnHeight);

        // selection highlight (animated bar behind the cursor row, colour = current HUD colour)
        if (selectedRow != -1) {
            Color primary = HUDModule.getHudColor();
            dl.color(primary.getRGB());
            float rowOffset = state.getRowOffset(selectedRow);
            float y = (float) (settings.rowHeight * rowBase) + topMargin + settings.rowHeight * rowOffset;
            float barX = childX + 2.0F;
            if (settings.gradient.getAsBoolean()) {
                // a 12 px solid bar, then a gradient bar from the HUD colour to the secondary colour (or a 65 % variant of it)
                Color secondary = HUDModule.isSecondaryActive()
                        ? ColorUtil.secondary(primary)
                        : ColorUtil.scale(primary, 0.65F);
                dl.rect(barX, y, 12.0F, (float) settings.rowHeight);
                dl.add(new GradientRectCommand(barX + 12.0F, y, width - 12.0F, (float) settings.rowHeight,
                        primary.getRGB(), secondary.getRGB()));
            } else {
                dl.rect(barX, y, width, (float) settings.rowHeight);
            }
        }

        // recurse into every visible child
        int hiddenBefore = 0;
        for (int i = 0; i < children.size(); i++) {
            GuiNode child = children.get(i);
            if (child.isHidden()) {
                hiddenBefore++;
            } else {
                this.layoutNode(dl, child, depth + 1, i, rowBase + i - hiddenBefore, width, childX);
            }
        }
    }

    /**
     * Vertical start of the menu: below the watermark. 9.5 when HUD > Other > Watermark is on, otherwise 2.0.
     */
    private static float topMargin() {
        BooleanSetting watermark = HUDModule.INSTANCE_ALT.watermark;
        return watermark.getAsBoolean() ? 9.5F : 2.0F;
    }

    // ================================================================================================================
    // Tree construction
    // ================================================================================================================

    private GuiNode buildTree() {
        RootNode rootNode = new RootNode();
        for (Category category : Category.values()) {                  // 5 categories
            CategoryComponent categoryNode = new CategoryComponent(category);   // CategoryComponent, label "Undefined"
            for (Module module : category.modules) {
                ModuleComponent moduleNode = new ModuleComponent(module);
                module.field24 = moduleNode;
                categoryNode.addChild(moduleNode);
                moduleNode.addChild(new KeybindNode("Keybind", module.bind, null));
                this.buildSettingNodes(moduleNode, module.B_());
            }
            rootNode.addChild(categoryNode);
        }
        return rootNode;
    }

    private void buildSettingNodes(GuiNode parent, List<Setting<?>> settings) {
        for (Setting<?> setting : settings) {                // ArrayList iteration with modCount check
            String label = labelOf(setting);
            if (setting instanceof BooleanSetting) {
                parent.addChild(new BooleanComponent(label, (BooleanSetting) setting));
            } else if (setting instanceof NumberSetting) {
                NumberComponent valueRow = new NumberComponent((NumberSetting) setting);
                parent.addChild(valueRow);
                valueRow.addChild(new SliderComponent(label, (NumberSetting) setting));
            } else if (setting instanceof ModeSetting) {
                ModeComponent modeRow = new ModeComponent(label, (ModeSetting<?>) setting);
                parent.addChild(modeRow);
                for (ModeOption option : ((ModeSetting<?>) setting).getOptions()) {
                    modeRow.addChild(new ModeOptionComponent(option.getName(), (ModeSetting<?>) setting, option));
                }
            } else if (setting instanceof ExpandSetting) {
                ExpandComponent groupRow = new ExpandComponent(label, (ExpandSetting) setting);
                parent.addChild(groupRow);
                this.buildSettingNodes(groupRow, ((ExpandSetting) setting).B_());
            } else if (setting instanceof StringSetting) {
                StringListComponent stringRow = new StringListComponent(label, (StringSetting) setting);
                parent.addChild(stringRow);
                stringRow.addChild(new StringListEditorComponent((StringSetting) setting));
            } else if (setting instanceof ColorSetting) {
                ColorComponent colorRow = new ColorComponent(label, (ColorSetting) setting);
                parent.addChild(colorRow);
            } else if (setting instanceof ButtonSetting) {
                parent.addChild(new ButtonComponent((ButtonSetting) setting));
            } else if (setting instanceof ItemsSetting      // includes BlocksSetting
                    || setting instanceof HotbarSetting
                    || setting instanceof EspPreviewSetting
                    || setting instanceof BedDefenseSetting) {
                // button "<label>..." that opens the setting's editor screen
                parent.addChild(new ButtonComponent(label, () -> ScreenUtil.openClickGuiAt(setting)));
            } else if (setting instanceof KeybindSetting) {
                parent.addChild(new KeybindNode(label, (com.adjustclient.core.bind.Bind) setting.getValue(), setting.hideIf));
            } else {
                Logger.log(LogLevel.WARN, true, "No tab node associated with property {} of type {}",
                        new Object[] { label, setting.getClass().getSimpleName() });
            }
        }
    }

    private static String labelOf(Setting<?> setting) {
        Supplier<String> supplier = setting.field36;
        if (supplier != null) {
            return supplier.get();
        }
        return setting.getName();
    }

    // ================================================================================================================
    // Keybind refresh
    // ================================================================================================================

    public void refreshKeybindNodes(Object event) {
        if (this.root == null) {
            return;
        }
        if (!this.isEnabled()) {
            return;
        }
        RefreshKeybindNodes consumer = new RefreshKeybindNodes(event);
        for (GuiNode child : this.root.getChildren()) {
            if (child instanceof KeybindNode) {
                ((KeybindNode) child).onBindChanged(consumer.event);
            }
            this.visitKeybindNodes(consumer, child);
        }
    }

    /** Recursive helper: calls {@link KeybindNode#onBindChanged} on every keybind row below {@code node}. */
    public void visitKeybindNodes(RefreshKeybindNodes consumer, GuiNode node) {
        for (GuiNode child : node.getChildren()) {
            if (child instanceof KeybindNode) {
                ((KeybindNode) child).onBindChanged(consumer.event);
            }
            this.visitKeybindNodes(consumer, child);
        }
    }

    // ================================================================================================================
    // Helper callbacks
    // ================================================================================================================

    /** {@code () -> { idleTicks = 0; moveGrace = 0; }} (resets the auto-close counters). */
    public static final class ResetAutoCloseCounters implements Runnable {
        private final TabGui gui;
        public ResetAutoCloseCounters(TabGui gui) {
            this.gui = gui;
        }
        @Override
        public void run() {
            this.gui.moveGrace = 0;
            this.gui.idleTicks = 0;
        }
    }

    public static final class EnabledAndInWorld implements BooleanSupplier {
        private final TabGui gui;
        public EnabledAndInWorld(TabGui gui) {
            this.gui = gui;
        }
        @Override
        public boolean getAsBoolean() {
            if (!this.gui.settings.enabled.getAsBoolean()) {   // Enabled && !hideIf
                return false;
            }
            return Minecraft.getMinecraft().thePlayer != null;
        }
    }

    public static final class RefreshKeybindNodes implements Consumer<GuiNode> {
        final Object event;
        public RefreshKeybindNodes(Object event) {
            this.event = event;
        }
        @Override
        public void accept(GuiNode node) {
            if (node instanceof KeybindNode) {
                ((KeybindNode) node).onBindChanged(this.event);
            }
        }
    }

}
