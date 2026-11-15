package org.lwjgl.input;

import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;

/**
 * LWJGL 2 {@code Cursor} compatibility stub for Minecraft 1.8.9 sources.
 */
public class Cursor {
    public static final int CURSOR_ONE_BIT_TRANSPARENCY = 1;
    public static final int CURSOR_8_BIT_ALPHA = 2;
    public static final int CURSOR_ANIMATION = 4;

    public Cursor(
            int width,
            int height,
            int xHotspot,
            int yHotspot,
            int numImages,
            IntBuffer images,
            IntBuffer delays)
            throws LWJGLException {
    }

    public static int getMinCursorSize() {
        return 1;
    }

    public static int getMaxCursorSize() {
        return 64;
    }

    public static int getCapabilities() {
        return CURSOR_ONE_BIT_TRANSPARENCY | CURSOR_8_BIT_ALPHA;
    }

    public void destroy() {
    }
}
