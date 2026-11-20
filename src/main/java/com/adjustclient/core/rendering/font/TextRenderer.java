package com.adjustclient.core.rendering.font;

public interface TextRenderer {

    float getStringWidth(String text);

    float getStringWidth(String text, float scale);

    String trimStringToWidth(String text, float width, boolean reverse);

    /** Draws text with a drop shadow. */
    float drawString(String text, float x, float y, int color);

    /** Draws text with a drop shadow at the given scale (NvgFontRenderer ignores the scale). */
    float drawString(String text, float x, float y, int color, float scale);

    /** Draws text without a shadow. */
    void drawStringNoShadow(String text, float x, float y, int color);

    default void drawCenteredString(String text, float x, float y, int color) {
        this.drawString(text, x - this.getStringWidth(text) * 0.5F, y, color);
    }

    /** Draws text without a shadow, horizontally centred on {@code x}. */
    default void drawCenteredStringNoShadow(String text, float x, float y, int color) {
        this.drawStringNoShadow(text, x - this.getStringWidth(text, 1.0F) * 0.5F, y, color);
    }
}
