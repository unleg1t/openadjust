package com.adjustclient.event;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public final class MovementInputEvent extends Event {
    public Set<Class<?>> movementOwners = new HashSet<>();
    public double sneakMultiplier = 0.3D;
    public boolean originalJump;
    public boolean forward;
    public boolean back;
    public boolean left;
    public boolean right;
    public boolean jump;
    public boolean sneak;

    public MovementInputEvent(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean sneak) {
        this.forward = forward;
        this.back = back;
        this.left = left;
        this.right = right;
        this.jump = jump;
        this.originalJump = jump;
        this.sneak = sneak;
    }

    public static MovementInputEvent fromKeyboard() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        float forward = settings.keyBindForward.isKeyDown() ? 1.0f : 0.0f;
        if (settings.keyBindBack.isKeyDown()) forward -= 1.0f;
        float strafe = settings.keyBindLeft.isKeyDown() ? 1.0f : 0.0f;
        if (settings.keyBindRight.isKeyDown()) strafe -= 1.0f;
        MovementInputEvent event = new MovementInputEvent(forward > 0.0f, forward < 0.0f, strafe > 0.0f, strafe < 0.0f,
                settings.keyBindJump.isKeyDown(), settings.keyBindSneak.isKeyDown());
        event.sneakMultiplier = 0.0D;
        event.originalJump = false;
        return event;
    }

    public float[] getMovement() {
        float forward = this.forward ? 1.0f : 0.0f;
        if (back) forward -= 1.0f;
        float strafe = left ? 1.0f : 0.0f;
        if (right) strafe -= 1.0f;
        return new float[] { sneak ? forward * 0.3f : forward, sneak ? strafe * 0.3f : strafe };
    }
}
