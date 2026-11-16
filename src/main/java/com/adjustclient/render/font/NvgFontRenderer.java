package com.adjustclient.render.font;

import org.lwjgl.nanovg.NVGColor;

import java.nio.FloatBuffer;

import static org.lwjgl.nanovg.NanoVG.*;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.StringUtil;

public final class NvgFontRenderer implements TextRenderer {

    /** Formatting code characters; index = code number (0-15 colours, 16 k, 17 l, 18 m, 19 n, 20 o, 21 r). */
    private static final String FORMAT_CODES = "0123456789abcdefklmnor";

    private static final int[] COLOR_CODES = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private final FontFamily family;
    public final float size;
    public final boolean symbolFont;
    private Object field16;

    public NvgFontRenderer(FontFamily family, float size, boolean symbolFont) {
        this.family = family;
        this.size = size;
        this.symbolFont = symbolFont;
        this.field16 = null;
    }

    /** Measures {@code text} (formatting codes skipped, '§l' switches to the bold face). */
    @Override
    public float getStringWidth(String text) {
        float width = 0.0F;
        if (text == null) {
            return width;
        }
        if (ServiceUtil.ahmedElSisiMode) {
            text = ServiceUtil.substituteTextCharacters(text);
        }
        nvgFontFace(NvgContext.vg, this.family.regular);
        nvgFontSize(NvgContext.vg, this.size);
        StringBuilder segment = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c >= 0x700) {
                continue;
            }
            boolean flush;
            if (c == '§') {
                if (i >= length - 1) {
                    segment.append('§');
                } else {
                    int code = FORMAT_CODES.indexOf(text.charAt(i + 1));
                    if (code < 16 || code == 21) {
                        nvgFontFace(NvgContext.vg, this.family.regular);
                    } else if (code == 17) {
                        if (this.family.bold != null) {
                            nvgFontFace(NvgContext.vg, this.family.bold);
                        }
                    }
                    i++;
                }
                flush = true;
            } else {
                segment.append(c);
                flush = i == length - 1;
            }
            // note: the face of the next segment is already selected when the pending segment is measured
            if (flush && segment.length() != 0) {
                width += nvgTextBounds(NvgContext.vg, 0.0F, 0.0F, segment.toString(), (FloatBuffer) null);
                segment.setLength(0);
            }
        }
        return width;
    }

    @Override
    public float getStringWidth(String text, float scale) {
        if (text == null) {
            return 0.0F;
        }
        if (ServiceUtil.ahmedElSisiMode) {
            text = ServiceUtil.substituteTextCharacters(text);
        }
        nvgFontFace(NvgContext.vg, this.family.regular);
        nvgFontSize(NvgContext.vg, this.size);
        StringBuilder segment = new StringBuilder();
        float width = 0.0F;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c >= 0x700) {
                continue;
            }
            boolean flush;
            if (c == '§') {
                if (i >= length - 1) {
                    segment.append('§');
                } else {
                    int code = FORMAT_CODES.indexOf(text.charAt(i + 1));
                    if (code < 16 || code == 21) {
                        nvgFontFace(NvgContext.vg, this.family.regular);
                    } else if (code == 17) {
                        if (this.family.bold != null) {
                            nvgFontFace(NvgContext.vg, this.family.bold);
                        }
                    }
                    i++;
                }
                flush = true;
            } else {
                segment.append(c);
                flush = i == length - 1;
            }
            if (flush && segment.length() != 0) {
                width += nvgTextBounds(NvgContext.vg, 0.0F, 0.0F, segment.toString(), (FloatBuffer) null);
                segment.setLength(0);
            }
        }
        return width;
    }

    /**
     * Longest prefix of {@code text} not wider than {@code width} (vanilla FontRenderer.trimStringToWidth logic with
     * per-character NanoVG widths). The reverse parameter of the interface method is not used here.
     */
    @Override
    public String trimStringToWidth(String text, float width, boolean reverse) {
        StringBuilder result = new StringBuilder();
        float current = 0.0F;
        boolean afterSection = false;   // previous char was '§'
        boolean formatFlag = false;     // vanilla "flag" (set for chars of negative width)
        boolean bold = false;
        for (int i = 0; i >= 0 && i < text.length() && current < width; i++) {
            char c = text.charAt(i);
            if (c == '§') {
                afterSection = true;
            } else if (afterSection) {
                afterSection = false;
            } else {
                float charWidth = this.getStringWidth(String.valueOf(c));
                if (formatFlag) {
                    formatFlag = false;
                    if (c != 'l' && c != 'L') {
                        if (c == 'r' || c == 'R') {
                            bold = false;
                        }
                    } else {
                        bold = true;
                    }
                } else if (charWidth < 0.0F) {
                    formatFlag = true;
                } else {
                    current += charWidth;
                    if (bold) {
                        ++current;
                    }
                }
                if (current > width) {
                    break;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    @Override
    public float drawString(String text, float x, float y, int color) {
        this.renderString(text, x + 0.5F, y + 0.5F, color, true);
        return this.renderString(text, x, y, color, false);
    }

    /** The scale parameter of the interface method is not used here. */
    @Override
    public float drawString(String text, float x, float y, int color, float scale) {
        return this.drawString(text, x, y, color);
    }

    @Override
    public void drawStringNoShadow(String text, float x, float y, int color) {
        this.renderString(text, x, y, color, false);
    }

    /** Renders one pass of {@code text}; returns the x position after the last character. */
    public float renderString(String text, float x, float y, int color, boolean shadow) {
        if (text == null) {
            return 0.0F;
        }
        if (ServiceUtil.ahmedElSisiMode) {
            text = ServiceUtil.substituteTextCharacters(text);
        }
        if (shadow) {
            color = color & 0xFF000000 | (color & 0xFCFCFC) >> 2;
        }
        nvgFontFace(NvgContext.vg, this.family.regular);
        nvgFontSize(NvgContext.vg, this.size);
        int alpha = color >>> 24;
        NVGColor baseColor = NvgContext.color(color);
        nvgFillColor(NvgContext.vg, baseColor);
        StringBuilder segment = new StringBuilder();
        boolean obfuscated = false;     // §k
        boolean strikethrough = false;  // §m
        boolean underline = false;      // §n
        boolean italic = false;         // §o
        boolean bold = false;           // §l
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                if (segment.length() != 0) {
                    this.drawSegment(segment.toString(), x, y, this.size, underline, strikethrough);
                    x += nvgTextBounds(NvgContext.vg, 0.0F, 0.0F, segment.toString(), (FloatBuffer) null);
                    segment.setLength(0);
                }
                int code = FORMAT_CODES.indexOf(Character.toLowerCase(text.charAt(++i)));
                if (code >= 0) {
                    if (code < 16) {
                        nvgFontFace(NvgContext.vg, this.family.regular);
                        int rgb = COLOR_CODES[code];
                        if (shadow) {
                            rgb = rgb & 0xFF000000 | (rgb & 0xFCFCFC) >> 2;
                        }
                        NVGColor codeColor = NvgContext.color(alpha << 24 | rgb & 0xFFFFFF);
                        nvgFillColor(NvgContext.vg, codeColor);
                        codeColor.free();
                        obfuscated = false;
                        italic = false;
                        bold = false;
                    } else if (code == 16) {
                        obfuscated = true;
                    } else if (code == 17) {
                        if (this.family.bold != null) {
                            nvgFontFace(NvgContext.vg, italic && this.family.boldItalic != null ? this.family.boldItalic : this.family.bold);
                        }
                        bold = true;
                    } else if (code == 18) {
                        strikethrough = true;
                    } else if (code == 19) {
                        underline = true;
                    } else if (code == 20) {
                        if (this.family.italic != null) {
                            nvgFontFace(NvgContext.vg, bold && this.family.boldItalic != null ? this.family.boldItalic : this.family.italic);
                        }
                        italic = true;
                    } else {
                        nvgFontFace(NvgContext.vg, this.family.regular);
                        nvgFillColor(NvgContext.vg, baseColor);
                        obfuscated = false;
                        italic = false;
                        bold = false;
                    }
                }
            } else {
                if (obfuscated) {
                    c = StringUtil.randomCharacter();
                }
                segment.append(c);
            }
        }
        if (segment.length() != 0) {
            this.drawSegment(segment.toString(), x, y, this.size, underline, strikethrough);
            x += nvgTextBounds(NvgContext.vg, 0.0F, 0.0F, segment.toString(), (FloatBuffer) null);
        }
        baseColor.free();
        return x;
    }

    /** Draws one run of identically formatted text and its underline / strikethrough. */
    private void drawSegment(String text, float x, float y, float size, boolean underline, boolean strikethrough) {
        nvgText(NvgContext.vg, x, y + size - 2.5F, text);
        float width = nvgTextBounds(NvgContext.vg, 0.0F, 0.0F, text, (FloatBuffer) null);
        if (strikethrough) {
            float lineY = size * 0.15F + y;
            nvgBeginPath(NvgContext.vg);
            nvgMoveTo(NvgContext.vg, x, lineY);
            nvgLineTo(NvgContext.vg, x + (width + 0.25F), lineY + 0.5F);
            nvgFill(NvgContext.vg);
            nvgClosePath(NvgContext.vg);
        }
        if (underline) {
            float lineY = size * 0.5F + y + 1.0F;
            nvgBeginPath(NvgContext.vg);
            nvgMoveTo(NvgContext.vg, x, lineY);
            nvgLineTo(NvgContext.vg, x + (width + 0.25F), lineY + 0.5F);
            nvgFill(NvgContext.vg);
            nvgClosePath(NvgContext.vg);
        }
    }
}
