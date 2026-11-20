package com.adjustclient.core.rendering.font;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import java.awt.Color;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class TextInput {
    public GuiTextField field;
    public Animation animation;
    public Function<String, String> transform;
    public String placeholder;
    public NvgFontRenderer font;
    public String[] lines;
    public boolean focused;
    public boolean hovered;

    public int lineCount() { return lines.length; }

    public TextInput(String placeholder, NvgFontRenderer font, int maxLength) {
        this.placeholder = placeholder;
        this.font = font;
        Minecraft mc = Minecraft.getMinecraft();
        field = new GuiTextField(0, mc != null ? mc.fontRendererObj : null, 0, 0, 1000, 9);
        field.setMaxStringLength(maxLength);
        field.setEnableBackgroundDrawing(false);
        animation = new Animation(Easing.LINEAR, 125L);
        setWidth(1000);
    }

    public void setWidth(int width) {
        String raw = field != null ? field.getText() : "";
        if (font == null) {
            String mapped = transform == null ? raw : transform.apply(raw);
            lines = new String[] { mapped };
            return;
        }
        List<IChatComponent> wrapped = GuiUtilRenderComponents.splitText(new ChatComponentText(raw),
                Math.max(width - 8, 5), font, false, false);
        lines = wrapped.stream().map(IChatComponent::getUnformattedText)
                .map(text -> transform == null ? text : transform.apply(text)).toArray(String[]::new);
    }

    public void draw(NvgRenderer renderer, float x, float y) {
        animation.update(focused ? (hovered ? 1.0F : 0.9F) : (hovered ? 0.4F : 0.0F));
        if (font == null || renderer == null) return;
        if (lines == null) setWidth(1000);
        String text = field.getText();
        if (!focused && text.isEmpty()) {
            if (placeholder != null) {
                int grey = (int) (30.0F * animation.value + 130.0F);
                renderer.color = new Color(grey, grey, grey).getRGB();
                renderer.text(font, placeholder, x, y);
            }
            return;
        }
        int color = ColorUtil.blend(new Color(199, 199, 199), new Color(149, 149, 149), animation.value).getRGB();
        int offset = 0;
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            int cursor = field.getCursorPosition();
            boolean bar = false;
            String shown = line;
            if (focused && field.cursorCounter / 6 % 2 == 0) {
                if (cursor == text.length()) {
                    if (i == lines.length - 1) shown = line + "\u00a7f_";
                } else bar = (offset == 0 || offset <= cursor) && offset + line.length() > cursor;
            }
            float lineY = y + i * 7.5F;
            renderer.color = color;
            renderer.text(font, shown, x, lineY);
            int end = offset + shown.length();
            if (focused) {
                int selection = field.getSelectionEnd();
                int from = Math.min(cursor, selection), to = Math.max(cursor, selection);
                if (cursor != selection && end > from && to > offset) {
                    renderer.color = new Color(228, 228, 228, 60).getRGB();
                    float sx = offset > from ? x : x + font.getStringWidth(text.substring(offset, from));
                    float width = font.getStringWidth(text.substring(Math.max(offset, from), Math.min(end, to)));
                    renderer.rect(sx, lineY - 1.5F, width, font.size);
                }
            }
            if (bar) {
                float cx = x + font.getStringWidth(text.substring(offset, cursor));
                renderer.pushEvenScale();
                renderer.color = new Color(166, 166, 166).getRGB();
                renderer.rect(cx, lineY - 1.5F, 0.5F, font.size);
                renderer.popEvenScale();
            }
            offset += shown.length();
        }
    }
}
