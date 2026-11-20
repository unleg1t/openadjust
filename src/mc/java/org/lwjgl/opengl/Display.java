package org.lwjgl.opengl;

import com.adjustclient.utilities.ClipboardUtil;
import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashSet;
import java.util.Set;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * LWJGL 2 {@code Display} bridge backed by a real LWJGL 3 GLFW window and OpenGL context.
 * Publishes the native GLFW window pointer to {@link ClipboardUtil#sharedWindowHandle}.
 */
public final class Display {
    private static final int DEFAULT_WIDTH = 854;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_STENCIL_BITS = 8;

    private static boolean glfwInitialized;
    private static GLFWErrorCallback errorCallback;

    private static long window;
    private static GLCapabilities contextCapabilities;
    private static boolean created;
    private static boolean closeRequested;
    private static boolean dirty;
    private static boolean fullscreen;
    private static boolean resizable;
    private static boolean focused = true;
    private static boolean iconified;
    private static boolean resized;

    private static String title = "Game";
    private static Canvas parent;
    private static DisplayMode initialMode;
    private static DisplayMode currentMode;

    private static int width = DEFAULT_WIDTH;
    private static int height = DEFAULT_HEIGHT;
    private static int windowWidth = DEFAULT_WIDTH;
    private static int windowHeight = DEFAULT_HEIGHT;
    private static int x = -1;
    private static int y = -1;

    private static int savedWindowX = 100;
    private static int savedWindowY = 100;
    private static int savedWindowWidth = DEFAULT_WIDTH;
    private static int savedWindowHeight = DEFAULT_HEIGHT;
    private static boolean hasSavedWindowBounds;

    private static int swapInterval;
    private static float initialRed;
    private static float initialGreen;
    private static float initialBlue;
    private static ByteBuffer[] cachedIcons;

    private static final Drawable DISPLAY_DRAWABLE = new GlfwDrawable();

    // Retained callback references so native trampolines stay alive for the window lifetime.
    private static GLFWWindowCloseCallback windowCloseCallback;
    private static GLFWWindowFocusCallback windowFocusCallback;
    private static GLFWWindowIconifyCallback windowIconifyCallback;
    private static GLFWWindowRefreshCallback windowRefreshCallback;
    private static GLFWWindowPosCallback windowPosCallback;
    private static GLFWWindowSizeCallback windowSizeCallback;
    private static GLFWFramebufferSizeCallback framebufferSizeCallback;

    // Frame-limiter state (LWJGL 2 Sync algorithm).
    private static final long NANOS_IN_SECOND = 1_000_000_000L;
    private static long nextFrameTime = -1L;
    private static boolean syncInitialized;
    private static final RunningAvg sleepDurations = new RunningAvg(10);
    private static final RunningAvg yieldDurations = new RunningAvg(10);

    private Display() {
    }

    public static long getWindowHandle() {
        return window;
    }

    public static double getScaleX() {
        return windowWidth > 0 ? (double) width / (double) windowWidth : 1.0;
    }

    public static double getScaleY() {
        return windowHeight > 0 ? (double) height / (double) windowHeight : 1.0;
    }

    private static void ensureGlfwInitialized() throws LWJGLException {
        if (!glfwInitialized) {
            if (errorCallback == null) {
                errorCallback = GLFWErrorCallback.createPrint(System.err);
                errorCallback.set();
            }
            if (!GLFW.glfwInit()) {
                throw new LWJGLException("Unable to initialize GLFW");
            }
            glfwInitialized = true;
        }
        if (initialMode == null) {
            long monitor = GLFW.glfwGetPrimaryMonitor();
            if (monitor != 0L) {
                GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
                if (vm != null) {
                    initialMode = toDisplayMode(vm);
                }
            }
            if (initialMode == null) {
                initialMode = new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT, 32, 60, true);
            }
            if (currentMode == null) {
                currentMode = initialMode;
                width = initialMode.getWidth();
                height = initialMode.getHeight();
                windowWidth = width;
                windowHeight = height;
            }
        }
    }

    private static void ensureGlfwInitializedUnchecked() {
        if (!glfwInitialized || initialMode == null) {
            try {
                ensureGlfwInitialized();
            } catch (LWJGLException e) {
                if (initialMode == null) {
                    initialMode = new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT, 32, 60, false);
                }
                if (currentMode == null) {
                    currentMode = initialMode;
                }
            }
        }
    }

    private static DisplayMode toDisplayMode(GLFWVidMode vm) {
        int rgbBits = vm.redBits() + vm.greenBits() + vm.blueBits();
        int bpp = rgbBits == 24 ? 32 : rgbBits;
        return new DisplayMode(vm.width(), vm.height(), bpp, vm.refreshRate(), true);
    }

    public static Drawable getDrawable() {
        return DISPLAY_DRAWABLE;
    }

    public static DisplayMode[] getAvailableDisplayModes() throws LWJGLException {
        ensureGlfwInitialized();
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) {
            return new DisplayMode[] { getDesktopDisplayMode() };
        }
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(monitor);
        if (modes == null) {
            return new DisplayMode[] { getDesktopDisplayMode() };
        }
        Set<DisplayMode> unique = new LinkedHashSet<>();
        for (int i = 0; i < modes.limit(); i++) {
            unique.add(toDisplayMode(modes.get(i)));
        }
        return unique.toArray(new DisplayMode[0]);
    }

    public static DisplayMode getDesktopDisplayMode() {
        ensureGlfwInitializedUnchecked();
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor != 0L) {
            GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
            if (vm != null) {
                initialMode = toDisplayMode(vm);
            }
        }
        return initialMode;
    }

    public static DisplayMode getDisplayMode() {
        ensureGlfwInitializedUnchecked();
        return currentMode;
    }

    public static void setDisplayMode(DisplayMode mode) throws LWJGLException {
        if (mode == null) {
            throw new NullPointerException("mode must be non-null");
        }
        ensureGlfwInitialized();
        if (mode.equals(currentMode)) {
            return;
        }
        currentMode = mode;
        if (created && window != 0L) {
            applyModeAndFullscreen();
        } else {
            DisplayMode eff = getEffectiveMode();
            width = eff.getWidth();
            height = eff.getHeight();
            windowWidth = width;
            windowHeight = height;
        }
    }

    public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(mode != null && mode.isFullscreenCapable(), mode);
    }

    private static void setDisplayModeAndFullscreenInternal(boolean fs, DisplayMode mode) throws LWJGLException {
        if (mode == null) {
            throw new NullPointerException("mode must be non-null");
        }
        ensureGlfwInitialized();
        boolean fsChanged = fullscreen != fs;
        boolean modeChanged = !mode.equals(currentMode);
        if (!fsChanged && !modeChanged) {
            return;
        }
        fullscreen = fs;
        currentMode = mode;
        if (created && window != 0L) {
            applyModeAndFullscreen();
        } else {
            DisplayMode eff = getEffectiveMode();
            width = eff.getWidth();
            height = eff.getHeight();
            windowWidth = width;
            windowHeight = height;
        }
    }

    private static DisplayMode getEffectiveMode() {
        ensureGlfwInitializedUnchecked();
        DisplayMode base = currentMode != null ? currentMode : initialMode;
        if (base == null) {
            return new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT, 32, 60, false);
        }
        if (base.getBitsPerPixel() != 0 && base.getFrequency() != 0) {
            return base;
        }
        DisplayMode desktop = initialMode != null ? initialMode : getDesktopDisplayMode();
        return new DisplayMode(
                base.getWidth() > 0 ? base.getWidth() : desktop.getWidth(),
                base.getHeight() > 0 ? base.getHeight() : desktop.getHeight(),
                base.getBitsPerPixel() > 0 ? base.getBitsPerPixel() : desktop.getBitsPerPixel(),
                base.getFrequency() > 0 ? base.getFrequency() : desktop.getFrequency(),
                base.isFullscreenCapable());
    }

    private static void applyModeAndFullscreen() {
        if (window == 0L) {
            return;
        }
        DisplayMode eff = getEffectiveMode();
        long currentMonitor = GLFW.glfwGetWindowMonitor(window);
        if (fullscreen) {
            long monitor = GLFW.glfwGetPrimaryMonitor();
            if (currentMonitor == 0L) {
                captureWindowedBounds();
            }
            if (monitor != 0L) {
                int freq = eff.getFrequency() > 0 ? eff.getFrequency() : GLFW.GLFW_DONT_CARE;
                GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, eff.getWidth(), eff.getHeight(), freq);
            } else {
                GLFW.glfwSetWindowSize(window, eff.getWidth(), eff.getHeight());
            }
        } else {
            int targetW = eff.getWidth();
            int targetH = eff.getHeight();
            if (currentMonitor != 0L) {
                int targetX = hasSavedWindowBounds ? savedWindowX : centeredX(targetW);
                int targetY = hasSavedWindowBounds ? savedWindowY : centeredY(targetH);
                GLFW.glfwSetWindowMonitor(window, 0L, targetX, targetY, targetW, targetH, GLFW.GLFW_DONT_CARE);
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            } else {
                GLFW.glfwSetWindowSize(window, targetW, targetH);
            }
        }
        syncSizesFromWindow(true);
    }

    private static void captureWindowedBounds() {
        if (window == 0L) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(window, px, py);
            GLFW.glfwGetWindowSize(window, pw, ph);
            savedWindowX = px.get(0);
            savedWindowY = py.get(0);
            savedWindowWidth = Math.max(1, pw.get(0));
            savedWindowHeight = Math.max(1, ph.get(0));
            hasSavedWindowBounds = true;
        }
    }

    private static int centeredX(int winW) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) {
            return 64;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer mx = stack.mallocInt(1);
            IntBuffer my = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(monitor, mx, my);
            GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
            if (vm != null) {
                return mx.get(0) + Math.max(0, (vm.width() - winW) / 2);
            }
        }
        return 64;
    }

    private static int centeredY(int winH) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) {
            return 64;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer mx = stack.mallocInt(1);
            IntBuffer my = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(monitor, mx, my);
            GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
            if (vm != null) {
                return my.get(0) + Math.max(0, (vm.height() - winH) / 2);
            }
        }
        return 64;
    }

    private static void syncSizesFromWindow(boolean markResized) {
        if (window == 0L) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer fw = stack.mallocInt(1);
            IntBuffer fh = stack.mallocInt(1);
            IntBuffer ww = stack.mallocInt(1);
            IntBuffer wh = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, fw, fh);
            GLFW.glfwGetWindowSize(window, ww, wh);
            int fbW = fw.get(0);
            int fbH = fh.get(0);
            int winW = ww.get(0);
            int winH = wh.get(0);
            if (fbW > 0 && fbH > 0) {
                if (markResized && (fbW != width || fbH != height)) {
                    resized = true;
                }
                width = fbW;
                height = fbH;
            }
            if (winW > 0 && winH > 0) {
                windowWidth = winW;
                windowHeight = winH;
            }
        }
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void setFullscreen(boolean fs) throws LWJGLException {
        ensureGlfwInitialized();
        if (fullscreen == fs) {
            return;
        }
        fullscreen = fs;
        if (created && window != 0L) {
            applyModeAndFullscreen();
        }
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String newTitle) {
        title = newTitle != null ? newTitle : "";
        if (created && window != 0L) {
            GLFW.glfwSetWindowTitle(window, title);
        }
    }

    public static Canvas getParent() {
        return parent;
    }

    public static void setParent(Canvas newParent) throws LWJGLException {
        parent = newParent;
    }

    public static boolean isCloseRequested() {
        if (!created || window == 0L) {
            return false;
        }
        return closeRequested || GLFW.glfwWindowShouldClose(window);
    }

    public static boolean isVisible() {
        if (!created || window == 0L) {
            return false;
        }
        return !iconified && GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
    }

    public static boolean isActive() {
        if (!created || window == 0L) {
            return false;
        }
        return focused && !iconified;
    }

    public static boolean isDirty() {
        return dirty;
    }

    public static void processMessages() {
        if (!created || window == 0L) {
            return;
        }
        GLFW.glfwPollEvents();
    }

    public static void swapBuffers() throws LWJGLException {
        if (!created || window == 0L) {
            throw new IllegalStateException("Display not created");
        }
        GLFW.glfwSwapBuffers(window);
        dirty = false;
    }

    public static void update() {
        update(true);
    }

    public static void update(boolean processMessages) {
        if (!created || window == 0L) {
            throw new IllegalStateException("Display not created");
        }
        if (GLFW.glfwGetCurrentContext() == window) {
            GLFW.glfwSwapBuffers(window);
            dirty = false;
        }
        if (processMessages) {
            processMessages();
        }
    }

    public static void releaseContext() throws LWJGLException {
        if (window != 0L) {
            GLFW.glfwMakeContextCurrent(0L);
            GLContext.useContext(null);
        }
    }

    public static boolean isCurrent() throws LWJGLException {
        return window != 0L && GLFW.glfwGetCurrentContext() == window;
    }

    public static void makeCurrent() throws LWJGLException {
        if (window == 0L) {
            throw new LWJGLException("Display not created");
        }
        GLFW.glfwMakeContextCurrent(window);
        if (contextCapabilities == null) {
            contextCapabilities = GL.createCapabilities();
        } else {
            GL.setCapabilities(contextCapabilities);
        }
    }

    public static void create() throws LWJGLException {
        create(new PixelFormat());
    }

    public static void create(PixelFormat pixel_format) throws LWJGLException {
        create(pixel_format, (Drawable) null, (ContextAttribs) null);
    }

    public static void create(PixelFormat pixel_format, Drawable shared_drawable) throws LWJGLException {
        create(pixel_format, shared_drawable, (ContextAttribs) null);
    }

    public static void create(PixelFormat pixel_format, ContextAttribs attribs) throws LWJGLException {
        create(pixel_format, (Drawable) null, attribs);
    }

    public static void create(PixelFormat pixel_format, Drawable shared_drawable, ContextAttribs attribs)
            throws LWJGLException {
        if (created) {
            throw new IllegalStateException("Only one LWJGL Display may be created");
        }
        if (pixel_format == null) {
            throw new NullPointerException("pixel_format must be non-null");
        }
        ensureGlfwInitialized();

        DisplayMode eff = getEffectiveMode();
        int targetW = Math.max(1, eff.getWidth());
        int targetH = Math.max(1, eff.getHeight());

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, pixel_format.getAlphaBits());
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, pixel_format.getDepthBits());
        // LWJGL 2 treats PixelFormat bit counts as minimums; never request fewer stencil bits than GLFW's
        // default (8) so NanoVG stencil fills keep working even when a caller leaves stencil at 0.
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, Math.max(DEFAULT_STENCIL_BITS, pixel_format.getStencilBits()));
        if (pixel_format.getSamples() > 0) {
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, pixel_format.getSamples());
        }
        if (pixel_format.isStereo()) {
            GLFW.glfwWindowHint(GLFW.GLFW_STEREO, GLFW.GLFW_TRUE);
        }
        if (pixel_format.isSRGB()) {
            GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, GLFW.GLFW_TRUE);
        }
        applyContextHints(attribs);

        long monitor = fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0L;
        if (monitor != 0L && eff.getFrequency() > 0) {
            GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, eff.getFrequency());
        }

        // The only Drawable implementation is this Display, which cannot exist before create(); there is
        // therefore never a GLFW window to share objects with and shared_drawable resolves to no share.
        window = GLFW.glfwCreateWindow(targetW, targetH, title != null ? title : "", monitor, 0L);
        if (window == 0L) {
            throw new LWJGLException("Failed to create the GLFW window");
        }

        if (monitor == 0L) {
            int posX = x >= 0 ? x : centeredX(targetW);
            int posY = y >= 0 ? y : centeredY(targetH);
            GLFW.glfwSetWindowPos(window, posX, posY);
            savedWindowX = posX;
            savedWindowY = posY;
            savedWindowWidth = targetW;
            savedWindowHeight = targetH;
            hasSavedWindowBounds = true;
        }

        installWindowCallbacks();
        applyCachedIcons();

        ClipboardUtil.sharedWindowHandle = window;

        GLFW.glfwMakeContextCurrent(window);
        contextCapabilities = GL.createCapabilities();
        GLFW.glfwSwapInterval(swapInterval);
        GL11.glClearColor(initialRed, initialGreen, initialBlue, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GLFW.glfwShowWindow(window);
        created = true;
        closeRequested = false;
        resized = false;
        focused = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        iconified = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        // Initialize from actual framebuffer pixels so HiDPI scales trigger an initial resize event.
        width = targetW;
        height = targetH;
        windowWidth = targetW;
        windowHeight = targetH;
        syncSizesFromWindow(true);

        try {
            Mouse.create();
            Keyboard.create();
        } catch (LWJGLException e) {
            destroy();
            throw e;
        }
    }

    private static void applyContextHints(ContextAttribs attribs) {
        if (attribs == null) {
            return;
        }
        int major = attribs.getMajorVersion();
        int minor = attribs.getMinorVersion();
        boolean requestsProfile = attribs.isProfileCore() || attribs.isProfileCompatibility();
        if (attribs.isProfileES()) {
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
            if (major > 0) {
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, major);
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, minor);
            }
        } else {
            if (major > 1 || (major == 1 && minor > 0) || requestsProfile || attribs.isForwardCompatible()) {
                // GLFW rejects profile/forward-compat hints when requested GL version is below 3.2.
                int effMajor = major;
                int effMinor = minor;
                if ((requestsProfile || attribs.isForwardCompatible()) && (major < 3 || (major == 3 && minor < 2))) {
                    effMajor = 3;
                    effMinor = 2;
                }
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, effMajor);
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, effMinor);
            }
            if (attribs.isProfileCore()) {
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            } else if (attribs.isProfileCompatibility()) {
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
            }
            if (attribs.isForwardCompatible()) {
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
            }
        }
        if (attribs.isDebug()) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }
        if (attribs.isRobustAccess()) {
            GLFW.glfwWindowHint(
                    GLFW.GLFW_CONTEXT_ROBUSTNESS,
                    attribs.isLoseContextOnReset()
                            ? GLFW.GLFW_LOSE_CONTEXT_ON_RESET
                            : GLFW.GLFW_NO_RESET_NOTIFICATION);
        }
        if (attribs.getContextReleaseBehavior() == ContextAttribs.CONTEXT_RELEASE_BEHAVIOR_NONE_ARB) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_RELEASE_BEHAVIOR, GLFW.GLFW_RELEASE_BEHAVIOR_NONE);
        } else if (attribs.getContextReleaseBehavior() == ContextAttribs.CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_RELEASE_BEHAVIOR, GLFW.GLFW_RELEASE_BEHAVIOR_FLUSH);
        }
    }

    public static void create(PixelFormatLWJGL pixel_format) throws LWJGLException {
        create(pixel_format instanceof PixelFormat ? (PixelFormat) pixel_format : new PixelFormat());
    }

    public static void create(PixelFormatLWJGL pixel_format, Drawable shared_drawable) throws LWJGLException {
        create(pixel_format instanceof PixelFormat ? (PixelFormat) pixel_format : new PixelFormat(), shared_drawable);
    }

    private static void installWindowCallbacks() {
        windowCloseCallback = GLFW.glfwSetWindowCloseCallback(window, win -> closeRequested = true);
        windowFocusCallback = GLFW.glfwSetWindowFocusCallback(window, (win, isFocused) -> focused = isFocused);
        windowIconifyCallback = GLFW.glfwSetWindowIconifyCallback(window, (win, isIconified) -> iconified = isIconified);
        windowRefreshCallback = GLFW.glfwSetWindowRefreshCallback(window, win -> dirty = true);
        windowPosCallback = GLFW.glfwSetWindowPosCallback(window, (win, xpos, ypos) -> {
            x = xpos;
            y = ypos;
            if (!fullscreen) {
                savedWindowX = xpos;
                savedWindowY = ypos;
            }
        });
        windowSizeCallback = GLFW.glfwSetWindowSizeCallback(window, (win, w, h) -> {
            if (w > 0 && h > 0) {
                windowWidth = w;
                windowHeight = h;
                if (!fullscreen) {
                    savedWindowWidth = w;
                    savedWindowHeight = h;
                    hasSavedWindowBounds = true;
                }
            }
        });
        framebufferSizeCallback = GLFW.glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            if (w > 0 && h > 0) {
                if (w != width || h != height) {
                    resized = true;
                }
                width = w;
                height = h;
            }
        });
    }

    public static void setInitialBackground(float red, float green, float blue) {
        initialRed = red;
        initialGreen = green;
        initialBlue = blue;
    }

    public static void destroy() {
        try {
            Mouse.destroy();
        } catch (Throwable ignored) {
        }
        try {
            Keyboard.destroy();
        } catch (Throwable ignored) {
        }

        ClipboardUtil.sharedWindowHandle = -1L;

        if (window != 0L) {
            try {
                Callbacks.glfwFreeCallbacks(window);
            } catch (Throwable ignored) {
            }
            GLFW.glfwDestroyWindow(window);
            window = 0L;
        }
        windowCloseCallback = null;
        windowFocusCallback = null;
        windowIconifyCallback = null;
        windowRefreshCallback = null;
        windowPosCallback = null;
        windowSizeCallback = null;
        framebufferSizeCallback = null;

        GL.setCapabilities(null);
        contextCapabilities = null;
        created = false;
        closeRequested = false;
        dirty = false;
        resized = false;

        if (glfwInitialized) {
            GLFW.glfwTerminate();
            glfwInitialized = false;
        }
        if (errorCallback != null) {
            errorCallback.free();
            errorCallback = null;
        }
    }

    public static boolean isCreated() {
        return created && window != 0L;
    }

    public static void setSwapInterval(int value) {
        swapInterval = value;
        if (created && window != 0L && GLFW.glfwGetCurrentContext() == window) {
            GLFW.glfwSwapInterval(swapInterval);
        }
    }

    public static void setVSyncEnabled(boolean sync) {
        setSwapInterval(sync ? 1 : 0);
    }

    public static void setLocation(int new_x, int new_y) {
        x = new_x;
        y = new_y;
        if (created && window != 0L && !fullscreen) {
            int tx = new_x == -1 ? centeredX(windowWidth) : new_x;
            int ty = new_y == -1 ? centeredY(windowHeight) : new_y;
            GLFW.glfwSetWindowPos(window, tx, ty);
        }
    }

    public static String getAdapter() {
        try {
            if (isCurrent()) {
                String vendor = GL11.glGetString(GL11.GL_VENDOR);
                return vendor != null ? vendor : "";
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    public static String getVersion() {
        try {
            if (isCurrent()) {
                String ver = GL11.glGetString(GL11.GL_VERSION);
                return ver != null ? ver : "";
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    public static int setIcon(ByteBuffer[] icons) {
        if (icons == null) {
            cachedIcons = null;
            return 0;
        }
        cachedIcons = new ByteBuffer[icons.length];
        for (int i = 0; i < icons.length; i++) {
            ByteBuffer src = icons[i];
            if (src != null) {
                ByteBuffer copy = ByteBuffer.allocate(src.remaining());
                ByteBuffer slice = src.slice();
                copy.put(slice);
                copy.flip();
                cachedIcons[i] = copy;
            }
        }
        if (created && window != 0L) {
            return applyCachedIcons();
        }
        return cachedIcons.length;
    }

    private static int applyCachedIcons() {
        if (window == 0L || cachedIcons == null || cachedIcons.length == 0) {
            return 0;
        }
        int validCount = 0;
        for (ByteBuffer buf : cachedIcons) {
            if (buf != null && buf.remaining() >= 4) {
                int pixels = buf.remaining() / 4;
                int side = (int) Math.round(Math.sqrt(pixels));
                if (side > 0 && side * side * 4 == buf.remaining()) {
                    validCount++;
                }
            }
        }
        if (validCount == 0) {
            return 0;
        }
        ByteBuffer[] nativeBuffers = new ByteBuffer[validCount];
        try (GLFWImage.Buffer images = GLFWImage.calloc(validCount)) {
            int idx = 0;
            for (ByteBuffer src : cachedIcons) {
                if (src == null || src.remaining() < 4) {
                    continue;
                }
                int rem = src.remaining();
                int side = (int) Math.round(Math.sqrt(rem / 4.0));
                if (side <= 0 || side * side * 4 != rem) {
                    continue;
                }
                ByteBuffer direct = MemoryUtil.memAlloc(rem);
                ByteBuffer slice = src.slice();
                direct.put(slice);
                direct.flip();
                nativeBuffers[idx] = direct;
                images.position(idx);
                images.width(side);
                images.height(side);
                images.pixels(direct);
                idx++;
            }
            images.position(0);
            GLFW.glfwSetWindowIcon(window, images);
            return validCount;
        } finally {
            for (ByteBuffer buf : nativeBuffers) {
                if (buf != null) {
                    MemoryUtil.memFree(buf);
                }
            }
        }
    }

    public static void setResizable(boolean newResizable) {
        if (resizable != newResizable) {
            resizable = newResizable;
            if (created && window != 0L) {
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            }
        }
    }

    public static boolean isResizable() {
        return resizable;
    }

    public static boolean wasResized() {
        boolean r = resized;
        resized = false;
        return r;
    }

    public static int getX() {
        if (fullscreen && (!created || window == 0L)) {
            return 0;
        }
        if (created && window != 0L) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer px = stack.mallocInt(1);
                IntBuffer py = stack.mallocInt(1);
                GLFW.glfwGetWindowPos(window, px, py);
                x = px.get(0);
            }
        }
        return x;
    }

    public static int getY() {
        if (fullscreen && (!created || window == 0L)) {
            return 0;
        }
        if (created && window != 0L) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer px = stack.mallocInt(1);
                IntBuffer py = stack.mallocInt(1);
                GLFW.glfwGetWindowPos(window, px, py);
                y = py.get(0);
            }
        }
        return y;
    }

    public static int getWidth() {
        if (fullscreen && currentMode != null && !created) {
            return currentMode.getWidth();
        }
        return width;
    }

    public static int getHeight() {
        if (fullscreen && currentMode != null && !created) {
            return currentMode.getHeight();
        }
        return height;
    }

    public static float getPixelScaleFactor() {
        return windowWidth > 0 ? (float) width / (float) windowWidth : 1.0F;
    }

    public static void sync(int fps) {
        if (fps <= 0) {
            return;
        }
        if (!syncInitialized) {
            syncInitialized = true;
            sleepDurations.init(1_000_000L);
            yieldDurations.init((long) (-(System.nanoTime() - System.nanoTime()) * 1.333));
            nextFrameTime = System.nanoTime();
            Thread timerHack = new Thread(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ignored) {
                }
            }, "LWJGL Timer");
            timerHack.setDaemon(true);
            timerHack.start();
        }

        try {
            for (long t0 = System.nanoTime(), t1;
                 (nextFrameTime - t0) > sleepDurations.avg();
                 t0 = t1) {
                Thread.sleep(1L);
                sleepDurations.add((t1 = System.nanoTime()) - t0);
            }
            sleepDurations.dampenForLowResTicker();
            for (long t0 = System.nanoTime(), t1;
                 (nextFrameTime - t0) > yieldDurations.avg();
                 t0 = t1) {
                Thread.yield();
                yieldDurations.add((t1 = System.nanoTime()) - t0);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        nextFrameTime = Math.max(nextFrameTime + NANOS_IN_SECOND / fps, System.nanoTime());
    }

    private static final class RunningAvg {
        private final long[] slots;
        private int offset;
        private static final long DAMPEN_THRESHOLD = 10_000_000L;
        private static final float DAMPEN_FACTOR = 0.9F;

        RunningAvg(int slotCount) {
            this.slots = new long[slotCount];
        }

        void init(long value) {
            for (int i = 0; i < slots.length; i++) {
                slots[i] = value;
            }
        }

        void add(long value) {
            slots[offset++ % slots.length] = value;
            offset %= slots.length;
        }

        long avg() {
            long sum = 0L;
            for (long slot : slots) {
                sum += slot;
            }
            return sum / slots.length;
        }

        void dampenForLowResTicker() {
            if (avg() > DAMPEN_THRESHOLD) {
                for (int i = 0; i < slots.length; i++) {
                    slots[i] = (long) (slots[i] * DAMPEN_FACTOR);
                }
            }
        }
    }

    private static final class GlfwDrawable implements Drawable {
        @Override
        public boolean isCurrent() throws LWJGLException {
            return Display.isCurrent();
        }

        @Override
        public void makeCurrent() throws LWJGLException {
            Display.makeCurrent();
        }

        @Override
        public void releaseContext() throws LWJGLException {
            Display.releaseContext();
        }

        @Override
        public void destroy() {
            Display.destroy();
        }

        @Override
        public void setCLSharingProperties(org.lwjgl.PointerBuffer properties) throws LWJGLException {
            throw new LWJGLException("OpenCL sharing properties are not supported");
        }
    }
}
