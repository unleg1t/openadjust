package com.adjustclient.core.gui.clickgui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.lwjgl.input.Keyboard;
import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.TextInput;
import com.adjustclient.core.rendering.font.TextInputBox;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.clickgui.setting.ExpandSettingComponent;
import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.screen.transition.DimTransition;
import com.adjustclient.utilities.ScreenUtil;
import com.adjustclient.utilities.Vec2f;

public final class SearchBar {
    DimTransition backdrop;
    TextInputBox textInput;
    ClickGuiScreen screen;
    Pattern pattern;
    ArrayList<Object> keywords;
    public boolean active;
    Object obf28;

    public SearchBar(ClickGuiScreen screen) {
        this.screen = screen;
        this.backdrop = new DimTransition(new com.adjustclient.core.rendering.animation.Animation(
                com.adjustclient.core.rendering.animation.Easing.LINEAR, 100L));
        this.textInput = new TextInputBox(new TextInput("", Fonts.TAHOMA_12, 32));
        this.textInput.input.setWidth(158);
        this.textInput.width = 158.0F;
        this.keywords = new ArrayList<>();
        compilePattern();
    }

    /** True when the component (or, for roots, one of its descendants) matches the current query. */
    public boolean matches(ClickGuiComponent c) {
        if (c.refreshPending) {
            return true;
        }
        if (!keywords.isEmpty()) {
            for (Object k : keywords) {
                if (c.equals(k)) {
                    return true;
                }
            }
        }
        String text = c.getSearchText();
        if (text != null && pattern.matcher(text).find()) {
            return true;
        }
        if (c.parent != null) {
            return false;
        }
        expandMatches(c.children);
        return c.refreshPending;
    }

    /** For every shown component of the list that matches: expand its ancestors; ExpandSetting rows additionally expand their children. Recurses into the children. */
    public void expandMatches(ArrayList<ClickGuiComponent> list) {
        if (list == null) {
            return;
        }
        for (ClickGuiComponent c : list) {
            if (c.shown) {
                String text = c.getSearchText();
                if (text != null && pattern.matcher(text).find()) {
                    c.expandAncestors();
                    if (c instanceof ExpandSettingComponent) {
                        for (ClickGuiComponent child : c.children) {
                            child.expandSelf();
                        }
                    }
                }
            }
            expandMatches(c.children);
        }
    }

    /** Key handler of the screen (typedChar, keyCode); true = consumed. */
    public boolean keyTyped(int typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN) {
            if (!active && ScreenUtil.canMoveInScreen()) {
                return false;
            }
            if (!active) {
                if (partyActive() && !GuiScreen.isCtrlKeyDown()) { // ENTER is taken by the party chat while a party exists: CTRL + ENTER then
                    return false;
                }
            }
            toggle();
            return true;
        }
        if (keyCode == Keyboard.KEY_ESCAPE && active) {
            toggle();
            return true;
        }
        if (!active) {
            return false;
        }
        textInput.input.focused = true;
        if (textInput.keyTyped((char) typedChar, keyCode)) {
            screen.layoutDirty = true;
            screen.collapseTemporaryRows();
            compilePattern();
            return true;
        }
        return false;
    }

    private static boolean partyActive() {
        return Adjust.INSTANCE.screenStack.partyService.party != null;
    }

    /** Draws the dim layer and the text input / hint. */
    public void render(Vec2f mouse, ScaledResolution window) {
        float dim;
        if (active) {
            if (isSearching()) {
                dim = textInput.input.focused ? 40.0F : 20.0F;
            } else {
                dim = 60.0F;
            }
        } else {
            dim = 0.0F;
        }
        backdrop.draw(window, dim);
        if (active) {
            textInput.input.placeholder = null;
        } else {
            String key = partyActive() ? "CTRL + ENTER" : "ENTER";
            textInput.input.placeholder = "Press " + ChatColor.WHITE + key + ChatColor.RESET + " to search...";
        }
        textInput.input.setWidth(158);
        textInput.width = 158.0F;
        float previousAlpha = NvgDraw.getGlobalAlpha();
        float dimValue = backdrop.animation.value;
        NvgDraw.setGlobalAlpha(Math.min(previousAlpha, Math.max(0.7F, dimValue)));
        // drawn centred horizontally, 13 px from the top: x = (windowWidth - input.width - closeButton.width) * 0.5, y = 13.0F
        float x = ((float) window.getScaledWidth() - textInput.width - (float) screen.homeButton.width) * 0.5F;
        Point2f at = new Point2f();
        at.x = mouse.x;
        at.y = mouse.y;
        textInput.draw(window, at, x, 13.0F);
        NvgDraw.setGlobalAlpha(previousAlpha);               // restores the previous alpha
    }

    public void collect(ClickGuiComponent c) {
        for (ClickGuiComponent child : c.children) {
            collect(child);
        }
        keywords.add(c);
    }

    public void toggle() {
        screen.layoutDirty = true;
        for (ClickGuiTab tab : screen.tabs) {
            ArrayList<ClickGuiComponent> entries = tab.getEntries();
            for (ClickGuiComponent e : new ArrayList<>(entries)) {
                if (e.refreshPending) {
                    e.setExpanded(false);
                }
            }
        }
        active = !active;
        textInput.input.field.setText("");                    // the query is emptied (GuiTextField text, cursor to the end)
        textInput.input.focused = active;
        compilePattern();
    }

    public boolean hasMatchingChild(ClickGuiComponent c) {
        ClickGuiTab tab = c.owner;
        if (tab == null) {
            return false;
        }
        for (ClickGuiComponent child : c.children) {
            if (tab.renderList.contains(child)) {
                return true;
            }
        }
        return false;
    }

    /** true while the field is open and contains text. */
    public boolean isSearching() {
        if (!active) {
            return false;
        }
        return textInput.input.field.getText().length() != 0;
    }

    public void compilePattern() {
        String quoted = Pattern.quote(textInput.input.field.getText());
        if (quoted.startsWith("\\Q") && quoted.endsWith("\\E")) {
            quoted = quoted.substring(2, quoted.length() - 2);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quoted.length(); i++) {
            sb.append(Pattern.quote(String.valueOf(quoted.charAt(i))));
            if (i < quoted.length() - 1) {
                sb.append("\\s*");
            }
        }
        this.pattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        this.keywords.clear();
    }
}
