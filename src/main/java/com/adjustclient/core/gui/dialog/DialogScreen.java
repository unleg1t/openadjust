package com.adjustclient.core.gui.dialog;

import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.rendering.font.TextInput;
import com.adjustclient.core.rendering.font.TextRenderer;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.screen.AdjustScreen;
import com.adjustclient.core.gui.screen.transition.PopupTransition;
import com.adjustclient.utilities.Vec2f;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;

public class DialogScreen extends AdjustScreen {
    public String title;
    public TextComponent[][] lines;
    public Vec2f size;
    public List<TextInputElement> textInputs;
    public List<AdjustButton> buttons;
    public Int2ObjectOpenHashMap<Consumer<DialogScreen>> actions;
    public Map<AdjustButton, Function<DialogScreen, Boolean>> validators;
    public List<DropdownElement> dropdowns;
    public Animation dim;
    public PopupTransition transition;
    public IChatComponent message;
    public float minimumWidth;
    public int bottomInset;
    public Object field60;

    public DialogScreen(String title, float minimumWidth, IChatComponent message,
            List<TextInputElement> textInputs, List<AdjustButton> buttons,
            Int2ObjectOpenHashMap<Consumer<DialogScreen>> actions,
            Map<AdjustButton, Function<DialogScreen, Boolean>> validators,
            List<DropdownElement> dropdowns, int bottomInset) {
        blocksGameInteraction = true;
        this.title = title;
        this.minimumWidth = minimumWidth;
        this.message = message;
        this.textInputs = textInputs;
        this.buttons = buttons;
        this.actions = actions;
        this.validators = validators;
        this.dropdowns = dropdowns;
        this.bottomInset = bottomInset;
        dim = new Animation(Easing.LINEAR, 150L);
        transition = new PopupTransition(this, new Animation(Easing.EASE_OUT_EXPO, 220L), 220L);
    }

    public boolean computeWidth(float width) {
        if (lines.length < 2) return false;
        float end = TextComponent.getWidth(lines[lines.length - 1], Fonts.TAHOMA_12) + 2.5F;
        for (AdjustButton button : buttons) {
            width -= button.getButtonWidth() + 5.0F;
            if (width - end < 15.0F) return false;
        }
        return true;
    }

    @Override public boolean keyTyped(char typedChar, int keyCode) {
        for (DropdownElement dropdown : dropdowns) {
            if (keyCode == Keyboard.KEY_ESCAPE && dropdown.expanded) {
                dropdown.expanded = false;
                return true;
            }
        }
        for (int i = 0; i < textInputs.size(); i++) {
            TextInput input = textInputs.get(i).box.input;
            if (!input.focused) continue;
            if (keyCode == Keyboard.KEY_TAB && textInputs.size() != 1) {
                input.focused = false;
                textInputs.get(i == textInputs.size() - 1 ? 0 : i + 1).box.input.focused = true;
                return true;
            }
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
                input.focused = false;
                return true;
            }
            input.field.setFocused(true);
            input.field.textboxKeyTyped(typedChar, keyCode);
            updateSize();
            return true;
        }
        if (keyCode != Keyboard.KEY_ESCAPE) return false;
        transition.close();
        return true;
    }

    public void setInputText(int ignoredIndex, String text) {
        for (TextInputElement element : textInputs) {
            if (element.index == 0) {
                element.box.input.field.setText(text);
                updateSize();
                return;
            }
        }
    }

    public void drawButtons(Vec2f mouse, float x, float y) {
        float right = x + size.x - 3.0F;
        for (AdjustButton button : buttons) {
            button.enabled = validators.get(button).apply(this);
            button.x = right - button.getButtonWidth();
            button.y = y + size.y - 13.0F;
            button.draw(mouse.x, mouse.y);
            right = button.x - 5.0F;
        }
    }

    public void render(Object context, Vec2f mouse, ScaledResolution window) {
        if (transition.getProgress()) return;
        layout();
        float x = window.getScaledWidth() / 2.0F - size.x / 2.0F;
        float y = window.getScaledHeight() / 2.0F - size.y / 2.0F;
        float previousAlpha = NvgDraw.getGlobalAlpha();
        NvgDraw.setGlobalAlpha(transition.animation.value);
        dim.update(115.0F);
        NvgDraw.fillRect(new Color(0, 0, 0, (int) dim.value).getRGB(), 0.0F, 0.0F,
                window.getScaledWidth(), window.getScaledHeight());
        NvgDraw.fillRect(0x9619191C, x, y, x + size.x, y + size.y);
        NvgDraw.strokeRect(true, 0x96181818, x, y, x + size.x, y + size.y, 0.5F);
        RenderUtil.drawAccentGradient(x, y, size.x, 10.0F, 0.0F, 0.0F);
        Fonts.TAHOMA_12.drawString(title, x + 2.5F, y + 3.5F, 0xF5C8C8C8);
        for (int i = 0; i < lines.length; i++) {
            TextComponent.draw(lines[i], Fonts.TAHOMA_12, x + 2.5F, y + 15.0F + i * 10.0F);
        }
        drawInputs(window, mouse, x, y);
        drawButtons(mouse, x, y);
        NvgDraw.setGlobalAlpha(previousAlpha);
    }

    public void drawInputs(ScaledResolution window, Vec2f mouse, float x, float y) {
        float rowY = y + 12.0F + lines.length * 10.0F;
        for (TextInputElement element : textInputs) {
            element.box.input.setWidth(element.width);
            element.box.width = element.width;
            Point2f point = new Point2f();
            point.x = mouse.x;
            point.y = mouse.y;
            element.box.draw(window, point, x + 2.5F, rowY);
            int count = element.box.input.lines.length;
            rowY += count >= 2 ? 10.5F + (count - 1) * 7.5F : 10.5F;
        }
        for (DropdownElement dropdown : dropdowns) {
            dropdown.draw(window, mouse, x + 2.5F, rowY, 70.0F);
            rowY += 11.0F;
        }
    }

    @Override public boolean mouseClicked(Vec2f mouse, int button, boolean pressed, ScaledResolution window) {
        if (!pressed || button != 0) return true;
        if (lines == null) layout();
        for (DropdownElement dropdown : dropdowns) {
            if (dropdown.mouseClicked(mouse)) {
                for (TextInputElement element : textInputs) element.box.input.focused = false;
                return true;
            }
        }
        for (AdjustButton actionButton : buttons) {
            if (actionButton.enabled && actionButton.mousePressed(net.minecraft.client.Minecraft.getMinecraft(),
                    (int) mouse.x, (int) mouse.y) && validators.get(actionButton).apply(this)) {
                Consumer<DialogScreen> action = actions.get(actionButton.id);
                if (action != null) action.accept(this);
                transition.close();
                return true;
            }
        }
        float x = window.getScaledWidth() / 2.0F - size.x / 2.0F + 2.5F;
        float rowY = window.getScaledHeight() / 2.0F - size.y / 2.0F + 12.0F + lines.length * 10.0F;
        TextInput focused = null;
        for (TextInputElement element : textInputs) {
            int count = element.box.input.lines.length;
            float height = 10.5F + (count >= 2 ? (count - 1) * 7.5F : 0.0F);
            if (mouse.x >= x && mouse.x <= x + element.width && mouse.y >= rowY && mouse.y <= rowY + height) {
                focused = element.box.input;
                break;
            }
            rowY += height;
        }
        for (TextInputElement element : textInputs) element.box.input.focused = false;
        if (focused != null) focused.focused = true;
        return true;
    }

    @Override public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        return mouseClicked(mouse, button, pressed,
                new ScaledResolution(mc, com.adjustclient.modules.impl.render.hud.HUDModule.getClickGuiScale()));
    }

    public void updateSize() {
        if (lines == null) return;
        float width = Math.max(Math.min(Math.max(Fonts.TAHOMA_12.getStringWidth(message.getFormattedText()),
                Fonts.TAHOMA_12.getStringWidth(title) + 4.5F), 170.0F), minimumWidth);
        boolean inline = computeWidth(width);
        float inputHeight = 0.0F;
        for (TextInputElement element : textInputs) {
            element.box.input.setWidth(element.width);
            int count = element.box.input.lines.length;
            inputHeight += count >= 2 ? (count - 1) * 7.5F + 10.0F : 10.0F;
        }
        if (inputHeight > 0.0F) inputHeight += 10.0F;
        size = new Vec2f(width, inputHeight + (30.0F - bottomInset - (inline ? 11.0F : 4.0F)
                + lines.length * 10.0F) + dropdowns.size() * 11.0F);
    }

    public int selectedDropdownIndex() {
        DropdownElement dropdown = dropdowns.get(0);
        return dropdown.selected == null ? -1
                : dropdown.options.indexOf(dropdown.selected);
    }

    public void layout() {
        if (lines != null) return;
        float width = Math.max(Math.min(Fonts.TAHOMA_12.getStringWidth(message.getFormattedText()), 170.0F), minimumWidth);
        List<IChatComponent> wrapped = net.minecraft.client.gui.GuiUtilRenderComponents.splitText(
                message, (int) (width - 2.0F), Fonts.TAHOMA_12, false, false);
        lines = new TextComponent[wrapped.size()][];
        for (int i = 0; i < lines.length; i++) {
            List<IChatComponent> siblings = wrapped.get(i).getSiblings();
            lines[i] = new TextComponent[siblings.size()];
            for (int j = 0; j < siblings.size(); j++) {
                IChatComponent component = siblings.get(j);
                net.minecraft.util.ChatStyle style = component.getChatStyle();
                String text = net.minecraft.util.StringUtils.stripControlCodes(component.getUnformattedTextForChat());
                if (style != null && style.getItalic()) {
                    lines[i][j] = new TextComponent(text, new Color(211, 211, 207, 255).getRGB());
                } else if (style != null && style.getBold()) {
                    lines[i][j] = new com.adjustclient.core.rendering.font.AccentText(text);
                } else {
                    lines[i][j] = new TextComponent(text, new Color(199, 199, 199, 200).getRGB());
                }
            }
        }
        updateSize();
    }

    public TextInputElement findTextInput(int index) {
        for (TextInputElement element : textInputs) if (element.index == index) return element;
        return null;
    }

    public void e() {
        for (TextInputElement element : textInputs) element.box.input.field.updateCursorCounter();
    }

    public boolean hasFocusedInput() {
        for (TextInputElement element : textInputs) if (element.box.input.focused) return true;
        return false;
    }
}

