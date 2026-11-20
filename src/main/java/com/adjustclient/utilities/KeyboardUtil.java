package com.adjustclient.utilities;

public class KeyboardUtil {
    public static boolean isKeyDown() {
        return org.lwjgl.input.Keyboard.isCreated()
                && (org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LCONTROL)
                || org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_RCONTROL));
    }
    public static char getKeyCharacter(int key) {
        String name;
        try {
            name = org.lwjgl.input.Keyboard.getKeyName(key);
        } catch (IndexOutOfBoundsException ignored) {
            // Out-of-range key codes (negative or past the key table) have no name.
            return '\0';
        }
        if (name == null) return '\0'; // unmapped slot inside the key table
        if ("SLASH".equals(name)) return '/';
        return name.length() == 1 ? name.charAt(0) : '\0';
    }
}
