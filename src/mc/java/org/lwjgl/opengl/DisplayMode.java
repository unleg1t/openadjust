package org.lwjgl.opengl;

/**
 * LWJGL 2 {@code DisplayMode} value type compatible with LWJGL 3 GLFW video modes.
 */
public final class DisplayMode {
    private final int width;
    private final int height;
    private final int bpp;
    private final int freq;
    private final boolean fullscreen;

    public DisplayMode(int width, int height) {
        this(width, height, 0, 0, false);
    }

    DisplayMode(int width, int height, int bpp, int freq) {
        this(width, height, bpp, freq, false);
    }

    DisplayMode(int width, int height, int bpp, int freq, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        this.freq = freq;
        this.fullscreen = fullscreen;
    }

    public boolean isFullscreenCapable() {
        return fullscreen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitsPerPixel() {
        return bpp;
    }

    public int getFrequency() {
        return freq;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DisplayMode)) {
            return false;
        }
        DisplayMode dm = (DisplayMode) obj;
        return dm.width == width && dm.height == height && dm.bpp == bpp && dm.freq == freq;
    }

    @Override
    public int hashCode() {
        return width ^ height ^ bpp ^ freq;
    }

    @Override
    public String toString() {
        return width + " x " + height + " x " + bpp + " @" + freq + "Hz";
    }
}
