package com.adjustclient.ui.hud.watermark;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

public final class WatermarkStandalone {

    private WatermarkStandalone() {
    }

    // ------------------------------------------------------------------ values read by Watermark.render

    public static final String CLIENT_NAME = "Adjust";

    /** Position of the text in HUD units (the frame scale is applied by NanoVG): x and y of the first drawString call. */
    public static final float ORIGIN_X = 2.5F;
    public static final float ORIGIN_Y = 2.5F;

    /** Gap between the client name and the edition label (units). */
    public static final float EDITION_GAP = 1.0F;

    /** Font sizes as passed to Fonts: nominal size N means NanoVG font size N * 0.5. */
    public static final float NAME_NOMINAL_SIZE = 14.0F;      // nominal size of Fonts.TAHOMA_14, not a static field access
    public static final float EDITION_NOMINAL_SIZE = 10.0F;   // nominal size of Fonts.TAHOMA_10, not a static field access

    /** new Color(225, 228, 228): the "djust" part. */
    public static final int NAME_RGB = 0xE1E4E4;
    /** new Color(170, 170, 170): the edition label. */
    public static final int EDITION_RGB = 0xAAAAAA;

    public static final boolean FIRST_LETTER_BOLD = true;

    public static String username = "";

    public static String editionFor(String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "Admin": case "axyo": case "KCALS": case "Sexting": case "tofle": case "xTori":
                return "KCALS Edition";
            case "auth": case "Fatal": case "purity": case "wykt":
                return "Nonce Edition";
            case "billionaire":
                return "Tuk Tuk Tuk Sahur Edition";
            case "Brett":
                return "Femboy Edition";
            case "days":
                return "Junkie Edition";
            case "fernan":
                return "Beaner Edition";
            case "howl":
                return "Howl's Edition";
            case "Moonaw":
                return "Special Ed Edition";
            default:
                return null;
        }
    }

    /** What the watermark needs from a text renderer. {@link NanoVgText} is the client's own implementation. */
    public interface Text {
        /**
         * Draws {@code text} like NvgFontRenderer.drawString: one pass at (x + 0.5, y + 0.5) in a quarter-brightness colour, then the real pass.
         * Returns the x after the text. {@code nominalSize} is the Fonts size (14, 10); the NanoVG font size is half of it.
         */
        float drawString(String text, float x, float y, float nominalSize, boolean bold, int argb);
    }

    public static void render(Text text, int hudArgb) {
        if (CLIENT_NAME.isEmpty()) {
            return;
        }
        float y = ORIGIN_Y;
        float x = ORIGIN_X;
        x = text.drawString(CLIENT_NAME.substring(0, 1), x, y, NAME_NOMINAL_SIZE, FIRST_LETTER_BOLD, hudArgb);
        if (CLIENT_NAME.length() >= 2) {
            x = text.drawString(CLIENT_NAME.substring(1), x, y, NAME_NOMINAL_SIZE, false, 0xFF000000 | NAME_RGB);
        }
        String edition = editionFor(username);
        if (edition != null) {
            x += EDITION_GAP;
            text.drawString(edition, x, y, EDITION_NOMINAL_SIZE, false, 0xFF000000 | EDITION_RGB);
        }
    }

    /** The colour the watermark's first letter gets. */
    public static final class HudColor {

        public enum Mode {
            PLAIN,
            HUD_COLOR,
            ANIMATED
        }

        public static Integer overrideArgb = null;

        public static Mode mode = Mode.PLAIN;
        public static int storedArgb = 0xFF70B7C6;
        public static boolean animatedKeepsStoredAlpha = false;

        public static int current(long nowMs) {
            if (overrideArgb != null) {
                return overrideArgb;
            }
            return getColor(mode, storedArgb, animatedKeepsStoredAlpha, nowMs, 0);
        }

        public static int getColor(Mode mode, int stored, boolean keepAlpha, long nowMs, int hudArgb) {
            switch (mode) {
                case ANIMATED: {
                    int rgb = cycle(nowMs);
                    int alpha = keepAlpha ? (stored >>> 24) : 255;
                    return alpha == 255 ? rgb : (alpha << 24 | (rgb & 0xFFFFFF));
                }
                case HUD_COLOR:
                    return hudArgb;
                default:
                    return stored;
            }
        }

        public static int cycle(long nowMs) {
            float t = ((float) (nowMs % 5000L) + 5000.0F) / 5000.0F;          // 1.0 .. 2.0
            float s = (t - (float) Math.floor(t)) * 6.0F;
            int segment = (int) s;
            float f = s - (float) Math.floor(s);
            float rising = f * 0.45F;                                          // segments 1, 3, 5
            float falling = (1.0F - f) * 0.45F;                                // segments 0, 2, 4
            switch (segment) {
                case 0: return 0xFFCC0070 | channel(falling) << 8;
                case 1: return 0xFF00CC70 | channel(rising) << 16;
                case 2: return 0xFF70CC00 | channel(falling);
                case 3: return 0xFF7000CC | channel(rising) << 8;
                case 4: return 0xFF0070CC | channel(falling) << 16;
                case 5: return 0xFFCC7000 | channel(rising);
                default: return 0xFF000000;
            }
        }

        private static int channel(float u) {
            return (int) ((1.0F - u) * 0.8F * 255.0F + 0.5F);
        }
    }

    public static final class NanoVgText implements Text {
        private final long vg;
        private static final String REGULAR = "AdjustTahoma";
        private static final String BOLD = "AdjustTahomaBD";

        /** {@code regularTtf} / {@code boldOtf}: the file contents as direct ByteBuffers (must stay alive while the context lives). */
        public NanoVgText(long vg, ByteBuffer regularTtf, ByteBuffer boldOtf) {
            this.vg = vg;
            NanoVG.nvgCreateFontMem(vg, REGULAR, regularTtf, false);
            NanoVG.nvgCreateFontMem(vg, BOLD, boldOtf, false);
        }

        public void beginFrame(float displayWidth, float displayHeight, float scale) {
            NanoVG.nvgBeginFrame(vg, displayWidth, displayHeight, 2.0F);
            NanoVG.nvgScale(vg, scale, scale);
        }

        public void endFrame() {
            NanoVG.nvgEndFrame(vg);
        }

        @Override
        public float drawString(String text, float x, float y, float nominalSize, boolean bold, int argb) {
            pass(text, x + 0.5F, y + 0.5F, nominalSize, bold, argb & 0xFF000000 | (argb & 0xFCFCFC) >> 2);
            return pass(text, x, y, nominalSize, bold, argb);
        }

        /** One pass (NvgFontRenderer.renderString + drawSegment). Returns x + advance. */
        private float pass(String text, float x, float y, float nominalSize, boolean bold, int argb) {
            float size = nominalSize * 0.5F;
            NanoVG.nvgFontFace(vg, bold ? BOLD : REGULAR);
            NanoVG.nvgFontSize(vg, size);
            NVGColor color = NVGColor.calloc();
            NanoVG.nvgRGBA((byte) (argb >> 16), (byte) (argb >> 8), (byte) argb, (byte) (argb >>> 24), color);
            NanoVG.nvgFillColor(vg, color);
            color.free();
            NanoVG.nvgText(vg, x, y + size - 2.5F, text);
            return x + NanoVG.nvgTextBounds(vg, 0.0F, 0.0F, text, (FloatBuffer) null);
        }
    }
}
