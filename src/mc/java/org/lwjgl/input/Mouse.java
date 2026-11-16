package org.lwjgl.input;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.opengl.Display;
import org.lwjgl.system.MemoryStack;

/**
 * LWJGL 2 {@code Mouse} bridge on top of LWJGL 3 GLFW mouse button, cursor, enter, and scroll callbacks.
 * Keeps bottom-left origin coordinates and exposes public {@link #eventButton} and {@link #event_state}.
 */
public final class Mouse {
    public static final int EVENT_SIZE = 22;
    private static final int BUTTON_COUNT = 8;
    private static final int WHEEL_DELTA = 120;

    /** Exposed current event button (-1 for movement/scroll events); temporarily overridden by AutoClickerModule. */
    public static int eventButton = -1;
    /** Exposed current event pressed state; temporarily overridden by AutoClickerModule. */
    public static boolean event_state;

    private static boolean created;
    private static final String[] buttonName = new String[BUTTON_COUNT];
    private static final Map<String, Integer> buttonMap = new HashMap<>(BUTTON_COUNT * 2);

    private static final ArrayDeque<MouseEvent> eventQueue = new ArrayDeque<>(64);
    private static int x;
    private static int y;
    private static int dx;
    private static int dy;
    private static int dwheel;

    private static int event_dx;
    private static int event_dy;
    private static int event_dwheel;
    private static int event_x;
    private static int event_y;
    private static long event_nanos;

    private static int lastRawX;
    private static int lastRawY;
    private static boolean hasLastPos;
    private static boolean ignoreNextCursorDelta;
    private static double scrollRemainder;

    private static boolean isGrabbed;
    private static boolean insideWindow = true;
    private static boolean clipMouseCoordinatesToWindow = true;

    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWCursorPosCallback cursorPosCallback;
    private static GLFWScrollCallback scrollCallback;
    private static GLFWCursorEnterCallback cursorEnterCallback;

    static {
        for (int i = 0; i < BUTTON_COUNT; i++) {
            String name = "BUTTON" + i;
            buttonName[i] = name;
            buttonMap.put(name, i);
        }
    }

    private Mouse() {
    }

    private static final class MouseEvent {
        final int button;
        final boolean state;
        final int dx;
        final int dy;
        final int dwheel;
        final int x;
        final int y;
        final long nanos;

        MouseEvent(int button, boolean state, int dx, int dy, int dwheel, int x, int y, long nanos) {
            this.button = button;
            this.state = state;
            this.dx = dx;
            this.dy = dy;
            this.dwheel = dwheel;
            this.x = x;
            this.y = y;
            this.nanos = nanos;
        }
    }

    public static void create() throws LWJGLException {
        long window = Display.getWindowHandle();
        if (window == 0L || !Display.isCreated()) {
            throw new IllegalStateException("Display must be created prior to creating Mouse");
        }
        if (created) {
            return;
        }
        eventQueue.clear();
        resetEventState();
        dx = 0;
        dy = 0;
        dwheel = 0;
        scrollRemainder = 0.0;
        isGrabbed = false;
        hasLastPos = false;
        ignoreNextCursorDelta = false;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var cx = stack.mallocDouble(1);
            var cy = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(window, cx, cy);
            int initX = toLwjglX(cx.get(0));
            int initY = toLwjglY(cy.get(0));
            lastRawX = initX;
            lastRawY = initY;
            hasLastPos = true;
            x = clampX(initX);
            y = clampY(initY);
            insideWindow = initX >= 0 && initX < Display.getWidth() && initY >= 0 && initY < Display.getHeight();
        }

        mouseButtonCallback = GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button < 0 || button >= BUTTON_COUNT) {
                return;
            }
            boolean pressed = action == GLFW.GLFW_PRESS;
            eventQueue.addLast(new MouseEvent(button, pressed, 0, 0, 0, x, y, System.nanoTime()));
        });

        cursorPosCallback = GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            int rawX = toLwjglX(xpos);
            int rawY = toLwjglY(ypos);
            if (!hasLastPos || ignoreNextCursorDelta) {
                lastRawX = rawX;
                lastRawY = rawY;
                hasLastPos = true;
                ignoreNextCursorDelta = false;
                if (!isGrabbed) {
                    x = clampX(rawX);
                    y = clampY(rawY);
                }
                return;
            }
            int deltaX = rawX - lastRawX;
            int deltaY = rawY - lastRawY;
            lastRawX = rawX;
            lastRawY = rawY;
            if (isGrabbed) {
                // LWJGL 2 keeps the virtual cursor inside the window while grabbed; deltas stay raw.
                x = clampX(x + deltaX);
                y = clampY(y + deltaY);
                dx += deltaX;
                dy += deltaY;
            } else {
                x = clampX(rawX);
                y = clampY(rawY);
                dx = 0;
                dy = 0;
            }
            if (deltaX != 0 || deltaY != 0) {
                eventQueue.addLast(new MouseEvent(-1, false, deltaX, deltaY, 0, x, y, System.nanoTime()));
            }
        });

        scrollCallback = GLFW.glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollRemainder += yoffset * WHEEL_DELTA;
            int deltaWheel = (int) scrollRemainder;
            if (deltaWheel != 0) {
                scrollRemainder -= deltaWheel;
                dwheel += deltaWheel;
                eventQueue.addLast(new MouseEvent(-1, false, 0, 0, deltaWheel, x, y, System.nanoTime()));
            }
        });

        cursorEnterCallback = GLFW.glfwSetCursorEnterCallback(window, (win, entered) -> insideWindow = entered);

        created = true;
    }

    private static void resetEventState() {
        eventButton = -1;
        event_state = false;
        event_dx = 0;
        event_dy = 0;
        event_dwheel = 0;
        event_x = 0;
        event_y = 0;
        event_nanos = 0L;
    }

    private static int toLwjglX(double glfwX) {
        return Math.round((float) (glfwX * Display.getScaleX()));
    }

    private static int toLwjglY(double glfwY) {
        return Display.getHeight() - 1 - Math.round((float) (glfwY * Display.getScaleY()));
    }

    private static int clampX(int val) {
        if (!clipMouseCoordinatesToWindow) {
            return val;
        }
        int max = Math.max(0, Display.getWidth() - 1);
        return Math.max(0, Math.min(max, val));
    }

    private static int clampY(int val) {
        if (!clipMouseCoordinatesToWindow) {
            return val;
        }
        int max = Math.max(0, Display.getHeight() - 1);
        return Math.max(0, Math.min(max, val));
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        if (!created) {
            return;
        }
        long window = Display.getWindowHandle();
        if (isGrabbed && window != 0L) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
        if (window != 0L) {
            // Detach and release the native GLFW trampolines so a later create() on the same window does not leak them.
            try {
                GLFWMouseButtonCallback previous = GLFW.glfwSetMouseButtonCallback(window, (GLFWMouseButtonCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
            try {
                GLFWCursorPosCallback previous = GLFW.glfwSetCursorPosCallback(window, (GLFWCursorPosCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
            try {
                GLFWScrollCallback previous = GLFW.glfwSetScrollCallback(window, (GLFWScrollCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
            try {
                GLFWCursorEnterCallback previous = GLFW.glfwSetCursorEnterCallback(window, (GLFWCursorEnterCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
        }
        created = false;
        isGrabbed = false;
        eventQueue.clear();
        resetEventState();
        mouseButtonCallback = null;
        cursorPosCallback = null;
        scrollCallback = null;
        cursorEnterCallback = null;
    }

    public static void poll() {
        if (created && Display.getWindowHandle() != 0L) {
            GLFW.glfwPollEvents();
        }
    }

    public static boolean isButtonDown(int button) {
        if (!created || button < 0 || button >= BUTTON_COUNT) {
            return false;
        }
        long window = Display.getWindowHandle();
        if (window == 0L) {
            return false;
        }
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public static String getButtonName(int button) {
        if (button < 0 || button >= buttonName.length) {
            return null;
        }
        return buttonName[button];
    }

    public static int getButtonIndex(String buttonNameQuery) {
        Integer ret = buttonMap.get(buttonNameQuery);
        return ret == null ? -1 : ret;
    }

    public static boolean next() {
        if (!created) {
            throw new IllegalStateException("Mouse must be created before you can read events");
        }
        MouseEvent next = eventQueue.pollFirst();
        if (next != null) {
            eventButton = next.button;
            event_state = next.state;
            event_dx = next.dx;
            event_dy = next.dy;
            event_dwheel = next.dwheel;
            event_x = next.x;
            event_y = next.y;
            event_nanos = next.nanos;
            return true;
        }
        return false;
    }

    public static int getEventButton() {
        return eventButton;
    }

    public static boolean getEventButtonState() {
        return event_state;
    }

    public static int getEventDX() {
        return event_dx;
    }

    public static int getEventDY() {
        return event_dy;
    }

    public static int getEventX() {
        return event_x;
    }

    public static int getEventY() {
        return event_y;
    }

    public static int getEventDWheel() {
        return event_dwheel;
    }

    public static long getEventNanoseconds() {
        return event_nanos;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getDX() {
        if (!isGrabbed) {
            dx = 0;
            return 0;
        }
        int ret = dx;
        dx = 0;
        return ret;
    }

    public static int getDY() {
        if (!isGrabbed) {
            dy = 0;
            return 0;
        }
        int ret = dy;
        dy = 0;
        return ret;
    }

    public static int getDWheel() {
        int ret = dwheel;
        dwheel = 0;
        return ret;
    }

    public static int getButtonCount() {
        return BUTTON_COUNT;
    }

    public static boolean hasWheel() {
        return true;
    }

    public static boolean isGrabbed() {
        return isGrabbed;
    }

    public static void setGrabbed(boolean grab) {
        if (!created) {
            isGrabbed = grab;
            return;
        }
        long window = Display.getWindowHandle();
        if (window == 0L) {
            isGrabbed = grab;
            return;
        }
        boolean changed = isGrabbed != grab;
        isGrabbed = grab;
        if (changed) {
            dx = 0;
            dy = 0;
            ignoreNextCursorDelta = true;
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                    grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
            if (!grab) {
                double glfwX = x / Display.getScaleX();
                double glfwY = (Display.getHeight() - 1 - y) / Display.getScaleY();
                GLFW.glfwSetCursorPos(window, glfwX, glfwY);
            }
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var cx = stack.mallocDouble(1);
                var cy = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(window, cx, cy);
                lastRawX = toLwjglX(cx.get(0));
                lastRawY = toLwjglY(cy.get(0));
                hasLastPos = true;
            }
        }
    }

    public static void setCursorPosition(int new_x, int new_y) {
        if (!created) {
            throw new IllegalStateException("Mouse is not created");
        }
        long window = Display.getWindowHandle();
        x = clampX(new_x);
        y = clampY(new_y);
        dx = 0;
        dy = 0;
        ignoreNextCursorDelta = true;
        if (window != 0L && !isGrabbed) {
            double glfwX = x / Display.getScaleX();
            double glfwY = (Display.getHeight() - 1 - y) / Display.getScaleY();
            GLFW.glfwSetCursorPos(window, glfwX, glfwY);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var cx = stack.mallocDouble(1);
                var cy = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(window, cx, cy);
                lastRawX = toLwjglX(cx.get(0));
                lastRawY = toLwjglY(cy.get(0));
                hasLastPos = true;
            }
        }
    }

    public static boolean isClipMouseCoordinatesToWindow() {
        return clipMouseCoordinatesToWindow;
    }

    public static void setClipMouseCoordinatesToWindow(boolean clip) {
        clipMouseCoordinatesToWindow = clip;
    }

    public static boolean isInsideWindow() {
        return insideWindow;
    }

    public static void updateCursor() {
        // Managed by GLFW window cursor state.
    }
}
