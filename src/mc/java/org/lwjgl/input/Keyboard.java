package org.lwjgl.input;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.opengl.Display;

/**
 * LWJGL 2 {@code Keyboard} bridge on top of LWJGL 3 GLFW key and char callbacks.
 * Exposes public {@link KeyEvent} for Adjust UI components and preserves LWJGL 2 key codes.
 */
public final class Keyboard {
    public static final int EVENT_SIZE = 18;
    public static final int CHAR_NONE = 0;

    public static final int KEY_NONE = 0;
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_1 = 2;
    public static final int KEY_2 = 3;
    public static final int KEY_3 = 4;
    public static final int KEY_4 = 5;
    public static final int KEY_5 = 6;
    public static final int KEY_6 = 7;
    public static final int KEY_7 = 8;
    public static final int KEY_8 = 9;
    public static final int KEY_9 = 10;
    public static final int KEY_0 = 11;
    public static final int KEY_MINUS = 12;
    public static final int KEY_EQUALS = 13;
    public static final int KEY_BACK = 14;
    public static final int KEY_TAB = 15;
    public static final int KEY_Q = 16;
    public static final int KEY_W = 17;
    public static final int KEY_E = 18;
    public static final int KEY_R = 19;
    public static final int KEY_T = 20;
    public static final int KEY_Y = 21;
    public static final int KEY_U = 22;
    public static final int KEY_I = 23;
    public static final int KEY_O = 24;
    public static final int KEY_P = 25;
    public static final int KEY_LBRACKET = 26;
    public static final int KEY_RBRACKET = 27;
    public static final int KEY_RETURN = 28;
    public static final int KEY_LCONTROL = 29;
    public static final int KEY_A = 30;
    public static final int KEY_S = 31;
    public static final int KEY_D = 32;
    public static final int KEY_F = 33;
    public static final int KEY_G = 34;
    public static final int KEY_H = 35;
    public static final int KEY_J = 36;
    public static final int KEY_K = 37;
    public static final int KEY_L = 38;
    public static final int KEY_SEMICOLON = 39;
    public static final int KEY_APOSTROPHE = 40;
    public static final int KEY_GRAVE = 41;
    public static final int KEY_LSHIFT = 42;
    public static final int KEY_BACKSLASH = 43;
    public static final int KEY_Z = 44;
    public static final int KEY_X = 45;
    public static final int KEY_C = 46;
    public static final int KEY_V = 47;
    public static final int KEY_B = 48;
    public static final int KEY_N = 49;
    public static final int KEY_M = 50;
    public static final int KEY_COMMA = 51;
    public static final int KEY_PERIOD = 52;
    public static final int KEY_SLASH = 53;
    public static final int KEY_RSHIFT = 54;
    public static final int KEY_MULTIPLY = 55;
    public static final int KEY_LMENU = 56;
    public static final int KEY_SPACE = 57;
    public static final int KEY_CAPITAL = 58;
    public static final int KEY_F1 = 59;
    public static final int KEY_F2 = 60;
    public static final int KEY_F3 = 61;
    public static final int KEY_F4 = 62;
    public static final int KEY_F5 = 63;
    public static final int KEY_F6 = 64;
    public static final int KEY_F7 = 65;
    public static final int KEY_F8 = 66;
    public static final int KEY_F9 = 67;
    public static final int KEY_F10 = 68;
    public static final int KEY_NUMLOCK = 69;
    public static final int KEY_SCROLL = 70;
    public static final int KEY_NUMPAD7 = 71;
    public static final int KEY_NUMPAD8 = 72;
    public static final int KEY_NUMPAD9 = 73;
    public static final int KEY_SUBTRACT = 74;
    public static final int KEY_NUMPAD4 = 75;
    public static final int KEY_NUMPAD5 = 76;
    public static final int KEY_NUMPAD6 = 77;
    public static final int KEY_ADD = 78;
    public static final int KEY_NUMPAD1 = 79;
    public static final int KEY_NUMPAD2 = 80;
    public static final int KEY_NUMPAD3 = 81;
    public static final int KEY_NUMPAD0 = 82;
    public static final int KEY_DECIMAL = 83;
    public static final int KEY_F11 = 87;
    public static final int KEY_F12 = 88;
    public static final int KEY_F13 = 100;
    public static final int KEY_F14 = 101;
    public static final int KEY_F15 = 102;
    public static final int KEY_F16 = 103;
    public static final int KEY_F17 = 104;
    public static final int KEY_F18 = 105;
    public static final int KEY_KANA = 112;
    public static final int KEY_F19 = 113;
    public static final int KEY_CONVERT = 121;
    public static final int KEY_NOCONVERT = 123;
    public static final int KEY_YEN = 125;
    public static final int KEY_NUMPADEQUALS = 141;
    public static final int KEY_CIRCUMFLEX = 144;
    public static final int KEY_AT = 145;
    public static final int KEY_COLON = 146;
    public static final int KEY_UNDERLINE = 147;
    public static final int KEY_KANJI = 148;
    public static final int KEY_STOP = 149;
    public static final int KEY_AX = 150;
    public static final int KEY_UNLABELED = 151;
    public static final int KEY_NUMPADENTER = 156;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_SECTION = 167;
    public static final int KEY_NUMPADCOMMA = 179;
    public static final int KEY_DIVIDE = 181;
    public static final int KEY_SYSRQ = 183;
    public static final int KEY_RMENU = 184;
    public static final int KEY_FUNCTION = 196;
    public static final int KEY_PAUSE = 197;
    public static final int KEY_HOME = 199;
    public static final int KEY_UP = 200;
    public static final int KEY_PRIOR = 201;
    public static final int KEY_LEFT = 203;
    public static final int KEY_RIGHT = 205;
    public static final int KEY_END = 207;
    public static final int KEY_DOWN = 208;
    public static final int KEY_NEXT = 209;
    public static final int KEY_INSERT = 210;
    public static final int KEY_DELETE = 211;
    public static final int KEY_CLEAR = 218;
    public static final int KEY_LMETA = 219;
    public static final int KEY_LWIN = 219;
    public static final int KEY_RMETA = 220;
    public static final int KEY_RWIN = 220;
    public static final int KEY_APPS = 221;
    public static final int KEY_POWER = 222;
    public static final int KEY_SLEEP = 223;

    public static final int KEYBOARD_SIZE = 256;

    private static final String[] keyName = new String[KEYBOARD_SIZE];
    private static final Map<String, Integer> keyMap = new HashMap<>(256);
    private static final int keyCount;

    private static final int[] GLFW_TO_LWJGL = new int[GLFW.GLFW_KEY_LAST + 1];
    private static final int[] LWJGL_TO_GLFW = new int[KEYBOARD_SIZE];

    private static final ArrayDeque<KeyEvent> eventQueue = new ArrayDeque<>(64);
    private static KeyEvent currentEvent = new KeyEvent();
    private static KeyEvent pendingPressForChar;
    /** Set by a suppressed GLFW repeat so the character GLFW emits right after it is dropped as well. */
    private static boolean dropNextChar;
    private static final String UNNAMED_KEY_PREFIX = "Key";

    private static boolean created;
    private static boolean repeatEnabled;
    private static GLFWKeyCallback keyCallback;
    private static GLFWCharCallback charCallback;

    /**
     * Public raw key event payload used both by {@link Keyboard} and by Adjust UI {@code KeyEventHandler}.
     */
    public static class KeyEvent {
        public boolean consumed;
        public boolean state;
        public char character;
        public int key;
        public long nanos;
        public boolean repeat;

        public KeyEvent() {
        }

        public KeyEvent(char character, int key, boolean state, long nanos, boolean repeat) {
            this.character = character;
            this.key = key;
            this.state = state;
            this.nanos = nanos;
            this.repeat = repeat;
        }

        void copyFrom(KeyEvent src) {
            this.consumed = src.consumed;
            this.state = src.state;
            this.character = src.character;
            this.key = src.key;
            this.nanos = src.nanos;
            this.repeat = src.repeat;
        }

        void reset() {
            this.consumed = false;
            this.state = false;
            this.character = (char) CHAR_NONE;
            this.key = KEY_NONE;
            this.nanos = 0L;
            this.repeat = false;
        }
    }

    static {
        int count = 0;
        for (Field field : Keyboard.class.getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)
                    && field.getType() == int.class && field.getName().startsWith("KEY_")) {
                try {
                    int val = field.getInt(null);
                    String name = field.getName().substring(4);
                    keyName[val] = name;
                    keyMap.put(name, val);
                    count++;
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        keyCount = count;
        // KEY_LWIN/KEY_RWIN alias KEY_LMETA/KEY_RMETA; keep the canonical names regardless of reflection order.
        keyName[KEY_LMETA] = "LMETA";
        keyName[KEY_RMETA] = "RMETA";

        Arrays.fill(LWJGL_TO_GLFW, GLFW.GLFW_KEY_UNKNOWN);
        mapKey(GLFW.GLFW_KEY_SPACE, KEY_SPACE);
        mapKey(GLFW.GLFW_KEY_APOSTROPHE, KEY_APOSTROPHE);
        mapKey(GLFW.GLFW_KEY_COMMA, KEY_COMMA);
        mapKey(GLFW.GLFW_KEY_MINUS, KEY_MINUS);
        mapKey(GLFW.GLFW_KEY_PERIOD, KEY_PERIOD);
        mapKey(GLFW.GLFW_KEY_SLASH, KEY_SLASH);
        mapKey(GLFW.GLFW_KEY_0, KEY_0);
        mapKey(GLFW.GLFW_KEY_1, KEY_1);
        mapKey(GLFW.GLFW_KEY_2, KEY_2);
        mapKey(GLFW.GLFW_KEY_3, KEY_3);
        mapKey(GLFW.GLFW_KEY_4, KEY_4);
        mapKey(GLFW.GLFW_KEY_5, KEY_5);
        mapKey(GLFW.GLFW_KEY_6, KEY_6);
        mapKey(GLFW.GLFW_KEY_7, KEY_7);
        mapKey(GLFW.GLFW_KEY_8, KEY_8);
        mapKey(GLFW.GLFW_KEY_9, KEY_9);
        mapKey(GLFW.GLFW_KEY_SEMICOLON, KEY_SEMICOLON);
        mapKey(GLFW.GLFW_KEY_EQUAL, KEY_EQUALS);
        mapKey(GLFW.GLFW_KEY_A, KEY_A);
        mapKey(GLFW.GLFW_KEY_B, KEY_B);
        mapKey(GLFW.GLFW_KEY_C, KEY_C);
        mapKey(GLFW.GLFW_KEY_D, KEY_D);
        mapKey(GLFW.GLFW_KEY_E, KEY_E);
        mapKey(GLFW.GLFW_KEY_F, KEY_F);
        mapKey(GLFW.GLFW_KEY_G, KEY_G);
        mapKey(GLFW.GLFW_KEY_H, KEY_H);
        mapKey(GLFW.GLFW_KEY_I, KEY_I);
        mapKey(GLFW.GLFW_KEY_J, KEY_J);
        mapKey(GLFW.GLFW_KEY_K, KEY_K);
        mapKey(GLFW.GLFW_KEY_L, KEY_L);
        mapKey(GLFW.GLFW_KEY_M, KEY_M);
        mapKey(GLFW.GLFW_KEY_N, KEY_N);
        mapKey(GLFW.GLFW_KEY_O, KEY_O);
        mapKey(GLFW.GLFW_KEY_P, KEY_P);
        mapKey(GLFW.GLFW_KEY_Q, KEY_Q);
        mapKey(GLFW.GLFW_KEY_R, KEY_R);
        mapKey(GLFW.GLFW_KEY_S, KEY_S);
        mapKey(GLFW.GLFW_KEY_T, KEY_T);
        mapKey(GLFW.GLFW_KEY_U, KEY_U);
        mapKey(GLFW.GLFW_KEY_V, KEY_V);
        mapKey(GLFW.GLFW_KEY_W, KEY_W);
        mapKey(GLFW.GLFW_KEY_X, KEY_X);
        mapKey(GLFW.GLFW_KEY_Y, KEY_Y);
        mapKey(GLFW.GLFW_KEY_Z, KEY_Z);
        mapKey(GLFW.GLFW_KEY_LEFT_BRACKET, KEY_LBRACKET);
        mapKey(GLFW.GLFW_KEY_BACKSLASH, KEY_BACKSLASH);
        mapKey(GLFW.GLFW_KEY_RIGHT_BRACKET, KEY_RBRACKET);
        mapKey(GLFW.GLFW_KEY_GRAVE_ACCENT, KEY_GRAVE);
        mapKey(GLFW.GLFW_KEY_WORLD_1, KEY_SECTION);
        mapKey(GLFW.GLFW_KEY_ESCAPE, KEY_ESCAPE);
        mapKey(GLFW.GLFW_KEY_ENTER, KEY_RETURN);
        mapKey(GLFW.GLFW_KEY_TAB, KEY_TAB);
        mapKey(GLFW.GLFW_KEY_BACKSPACE, KEY_BACK);
        mapKey(GLFW.GLFW_KEY_INSERT, KEY_INSERT);
        mapKey(GLFW.GLFW_KEY_DELETE, KEY_DELETE);
        mapKey(GLFW.GLFW_KEY_RIGHT, KEY_RIGHT);
        mapKey(GLFW.GLFW_KEY_LEFT, KEY_LEFT);
        mapKey(GLFW.GLFW_KEY_DOWN, KEY_DOWN);
        mapKey(GLFW.GLFW_KEY_UP, KEY_UP);
        mapKey(GLFW.GLFW_KEY_PAGE_UP, KEY_PRIOR);
        mapKey(GLFW.GLFW_KEY_PAGE_DOWN, KEY_NEXT);
        mapKey(GLFW.GLFW_KEY_HOME, KEY_HOME);
        mapKey(GLFW.GLFW_KEY_END, KEY_END);
        mapKey(GLFW.GLFW_KEY_CAPS_LOCK, KEY_CAPITAL);
        mapKey(GLFW.GLFW_KEY_SCROLL_LOCK, KEY_SCROLL);
        mapKey(GLFW.GLFW_KEY_NUM_LOCK, KEY_NUMLOCK);
        mapKey(GLFW.GLFW_KEY_PRINT_SCREEN, KEY_SYSRQ);
        mapKey(GLFW.GLFW_KEY_PAUSE, KEY_PAUSE);
        mapKey(GLFW.GLFW_KEY_F1, KEY_F1);
        mapKey(GLFW.GLFW_KEY_F2, KEY_F2);
        mapKey(GLFW.GLFW_KEY_F3, KEY_F3);
        mapKey(GLFW.GLFW_KEY_F4, KEY_F4);
        mapKey(GLFW.GLFW_KEY_F5, KEY_F5);
        mapKey(GLFW.GLFW_KEY_F6, KEY_F6);
        mapKey(GLFW.GLFW_KEY_F7, KEY_F7);
        mapKey(GLFW.GLFW_KEY_F8, KEY_F8);
        mapKey(GLFW.GLFW_KEY_F9, KEY_F9);
        mapKey(GLFW.GLFW_KEY_F10, KEY_F10);
        mapKey(GLFW.GLFW_KEY_F11, KEY_F11);
        mapKey(GLFW.GLFW_KEY_F12, KEY_F12);
        mapKey(GLFW.GLFW_KEY_F13, KEY_F13);
        mapKey(GLFW.GLFW_KEY_F14, KEY_F14);
        mapKey(GLFW.GLFW_KEY_F15, KEY_F15);
        mapKey(GLFW.GLFW_KEY_F16, KEY_F16);
        mapKey(GLFW.GLFW_KEY_F17, KEY_F17);
        mapKey(GLFW.GLFW_KEY_F18, KEY_F18);
        mapKey(GLFW.GLFW_KEY_F19, KEY_F19);
        mapKey(GLFW.GLFW_KEY_KP_0, KEY_NUMPAD0);
        mapKey(GLFW.GLFW_KEY_KP_1, KEY_NUMPAD1);
        mapKey(GLFW.GLFW_KEY_KP_2, KEY_NUMPAD2);
        mapKey(GLFW.GLFW_KEY_KP_3, KEY_NUMPAD3);
        mapKey(GLFW.GLFW_KEY_KP_4, KEY_NUMPAD4);
        mapKey(GLFW.GLFW_KEY_KP_5, KEY_NUMPAD5);
        mapKey(GLFW.GLFW_KEY_KP_6, KEY_NUMPAD6);
        mapKey(GLFW.GLFW_KEY_KP_7, KEY_NUMPAD7);
        mapKey(GLFW.GLFW_KEY_KP_8, KEY_NUMPAD8);
        mapKey(GLFW.GLFW_KEY_KP_9, KEY_NUMPAD9);
        mapKey(GLFW.GLFW_KEY_KP_DECIMAL, KEY_DECIMAL);
        mapKey(GLFW.GLFW_KEY_KP_DIVIDE, KEY_DIVIDE);
        mapKey(GLFW.GLFW_KEY_KP_MULTIPLY, KEY_MULTIPLY);
        mapKey(GLFW.GLFW_KEY_KP_SUBTRACT, KEY_SUBTRACT);
        mapKey(GLFW.GLFW_KEY_KP_ADD, KEY_ADD);
        mapKey(GLFW.GLFW_KEY_KP_ENTER, KEY_NUMPADENTER);
        mapKey(GLFW.GLFW_KEY_KP_EQUAL, KEY_NUMPADEQUALS);
        mapKey(GLFW.GLFW_KEY_LEFT_SHIFT, KEY_LSHIFT);
        mapKey(GLFW.GLFW_KEY_LEFT_CONTROL, KEY_LCONTROL);
        mapKey(GLFW.GLFW_KEY_LEFT_ALT, KEY_LMENU);
        mapKey(GLFW.GLFW_KEY_LEFT_SUPER, KEY_LMETA);
        mapKey(GLFW.GLFW_KEY_RIGHT_SHIFT, KEY_RSHIFT);
        mapKey(GLFW.GLFW_KEY_RIGHT_CONTROL, KEY_RCONTROL);
        mapKey(GLFW.GLFW_KEY_RIGHT_ALT, KEY_RMENU);
        mapKey(GLFW.GLFW_KEY_RIGHT_SUPER, KEY_RMETA);
        mapKey(GLFW.GLFW_KEY_MENU, KEY_APPS);
    }

    private Keyboard() {
    }

    private static void mapKey(int glfwKey, int lwjglKey) {
        if (glfwKey >= 0 && glfwKey < GLFW_TO_LWJGL.length && lwjglKey >= 0 && lwjglKey < LWJGL_TO_GLFW.length) {
            GLFW_TO_LWJGL[glfwKey] = lwjglKey;
            LWJGL_TO_GLFW[lwjglKey] = glfwKey;
        }
    }

    public static int glfwToLwjgl(int glfwKey) {
        if (glfwKey >= 0 && glfwKey < GLFW_TO_LWJGL.length) {
            return GLFW_TO_LWJGL[glfwKey];
        }
        return KEY_NONE;
    }

    public static int lwjglToGlfw(int lwjglKey) {
        if (lwjglKey >= 0 && lwjglKey < LWJGL_TO_GLFW.length) {
            return LWJGL_TO_GLFW[lwjglKey];
        }
        return GLFW.GLFW_KEY_UNKNOWN;
    }

    public static void create() throws LWJGLException {
        long window = Display.getWindowHandle();
        if (window == 0L || !Display.isCreated()) {
            throw new IllegalStateException("Display must be created prior to creating Keyboard");
        }
        if (created) {
            return;
        }
        eventQueue.clear();
        currentEvent.reset();
        pendingPressForChar = null;
        dropNextChar = false;

        // GLFW delivers the char callback for a key immediately after the key callback that produced it
        // (same message/event on every backend), so a character is paired with the most recent key event.
        keyCallback = GLFW.glfwSetKeyCallback(window, (win, glfwKey, scancode, action, mods) -> {
            pendingPressForChar = null;
            dropNextChar = false;
            if (action == GLFW.GLFW_REPEAT && !repeatEnabled) {
                // LWJGL 2 drops suppressed repeats together with their character.
                dropNextChar = true;
                return;
            }
            int lwjglKey = glfwToLwjgl(glfwKey);
            boolean down = action != GLFW.GLFW_RELEASE;
            boolean repeat = action == GLFW.GLFW_REPEAT;
            KeyEvent ev = new KeyEvent((char) CHAR_NONE, lwjglKey, down, System.nanoTime(), repeat);
            eventQueue.addLast(ev);
            if (down) {
                pendingPressForChar = ev;
            }
        });

        charCallback = GLFW.glfwSetCharCallback(window, (win, codepoint) -> {
            if (dropNextChar) {
                dropNextChar = false;
                return;
            }
            char ch = (char) codepoint;
            if (pendingPressForChar != null && pendingPressForChar.character == CHAR_NONE) {
                pendingPressForChar.character = ch;
                pendingPressForChar = null;
            } else {
                // Character without a key event of its own (IME commit, dead-key compose, extra code units).
                eventQueue.addLast(new KeyEvent(ch, KEY_NONE, true, System.nanoTime(), false));
            }
        });

        created = true;
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        if (!created) {
            return;
        }
        long window = Display.getWindowHandle();
        if (window != 0L) {
            // Detach and release the native GLFW trampolines so a later create() on the same window does not leak them.
            try {
                GLFWKeyCallback previous = GLFW.glfwSetKeyCallback(window, (GLFWKeyCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
            try {
                GLFWCharCallback previous = GLFW.glfwSetCharCallback(window, (GLFWCharCallbackI) null);
                if (previous != null) {
                    previous.free();
                }
            } catch (Throwable ignored) {
            }
        }
        created = false;
        repeatEnabled = false;
        eventQueue.clear();
        currentEvent.reset();
        pendingPressForChar = null;
        dropNextChar = false;
        keyCallback = null;
        charCallback = null;
    }

    public static void poll() {
        if (created && Display.getWindowHandle() != 0L) {
            GLFW.glfwPollEvents();
        }
    }

    public static boolean isKeyDown(int key) {
        if (!created) {
            return false;
        }
        long window = Display.getWindowHandle();
        if (window == 0L) {
            return false;
        }
        int glfwKey = lwjglToGlfw(key);
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return GLFW.glfwGetKey(window, glfwKey) == GLFW.GLFW_PRESS;
    }

    /**
     * Returns the LWJGL 2 name of {@code key}; unmapped slots and out-of-range codes yield {@code "Key" + key}
     * (the same label Adjust bind UIs use) instead of {@code null} / {@link ArrayIndexOutOfBoundsException}.
     */
    public static synchronized String getKeyName(int key) {
        if (key >= 0 && key < keyName.length) {
            String name = keyName[key];
            if (name != null) {
                return name;
            }
        }
        return UNNAMED_KEY_PREFIX + key;
    }

    public static synchronized int getKeyIndex(String keyNameQuery) {
        if (keyNameQuery == null) {
            return KEY_NONE;
        }
        Integer ret = keyMap.get(keyNameQuery);
        if (ret != null) {
            return ret;
        }
        // Round-trip for the "Key<code>" fallback produced by getKeyName.
        int prefixLength = UNNAMED_KEY_PREFIX.length();
        if (keyNameQuery.length() > prefixLength
                && keyNameQuery.regionMatches(true, 0, UNNAMED_KEY_PREFIX, 0, prefixLength)) {
            try {
                int code = Integer.parseInt(keyNameQuery.substring(prefixLength));
                if (code >= 0 && code < KEYBOARD_SIZE) {
                    return code;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return KEY_NONE;
    }

    public static int getNumKeyboardEvents() {
        if (!created) {
            return 0;
        }
        return eventQueue.size();
    }

    public static boolean next() {
        if (!created) {
            throw new IllegalStateException("Keyboard must be created before you can read events");
        }
        pendingPressForChar = null;
        dropNextChar = false;
        KeyEvent next = eventQueue.pollFirst();
        if (next != null) {
            currentEvent.copyFrom(next);
            return true;
        }
        return false;
    }

    public static void enableRepeatEvents(boolean enable) {
        repeatEnabled = enable;
    }

    public static boolean areRepeatEventsEnabled() {
        return repeatEnabled;
    }

    public static int getKeyCount() {
        return keyCount;
    }

    public static char getEventCharacter() {
        return currentEvent.character;
    }

    public static int getEventKey() {
        return currentEvent.key;
    }

    public static boolean getEventKeyState() {
        return currentEvent.state;
    }

    public static long getEventNanoseconds() {
        return currentEvent.nanos;
    }

    public static boolean isRepeatEvent() {
        return currentEvent.repeat;
    }
}
