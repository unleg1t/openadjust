package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        if (com.adjustclient.module.impl.movement.SafeWalkModule.INSTANCE != null
            && (com.adjustclient.module.impl.movement.SafeWalkModule.INSTANCE.isEnabled()
                || com.adjustclient.module.impl.movement.SafeWalkModule.INSTANCE.cooldown != null))
        {
            this.updatePlayerMoveState(false);
            com.adjustclient.module.impl.movement.SafeWalkModule.INSTANCE.updateSneaking();
        }

        this.updatePlayerMoveState(true);
    }

    private void updatePlayerMoveState(boolean dispatchEvents)
    {
        boolean forward = this.gameSettings.keyBindForward.isKeyDown();
        boolean back = this.gameSettings.keyBindBack.isKeyDown();
        boolean left = this.gameSettings.keyBindLeft.isKeyDown();
        boolean right = this.gameSettings.keyBindRight.isKeyDown();
        boolean jump = this.gameSettings.keyBindJump.isKeyDown();
        boolean sneak = this.gameSettings.keyBindSneak.isKeyDown();

        com.adjustclient.event.MovementInputEvent event = new com.adjustclient.event.MovementInputEvent(
            forward, back, left, right, jump, sneak);

        if (dispatchEvents)
        {
            com.adjustclient.event.EventBus.post(event);
        }

        if (com.adjustclient.module.impl.render.freecam.FreecamModule.INSTANCE != null
            && com.adjustclient.module.impl.render.freecam.FreecamModule.INSTANCE.isEnabled()
            && !com.adjustclient.module.impl.render.freecam.FreecamModule.INSTANCE.isMouseDown())
        {
            event.forward = false;
            event.back = false;
            event.left = false;
            event.right = false;
            event.jump = false;
            event.sneak = false;
        }

        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (event.forward)
        {
            ++this.moveForward;
        }

        if (event.back)
        {
            --this.moveForward;
        }

        if (event.left)
        {
            ++this.moveStrafe;
        }

        if (event.right)
        {
            --this.moveStrafe;
        }

        this.jump = event.jump;
        this.sneak = event.sneak;

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * event.sneakMultiplier);
            this.moveForward = (float)((double)this.moveForward * event.sneakMultiplier);
        }
    }
}
