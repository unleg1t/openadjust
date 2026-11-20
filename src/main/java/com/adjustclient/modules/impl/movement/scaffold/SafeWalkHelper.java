package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.events.SafeWalkEvent;
import com.adjustclient.utilities.MinecraftInstance;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import com.adjustclient.utilities.RandomUtil;

public final class SafeWalkHelper implements MinecraftInstance, Listener {

    private final ScaffoldModule module;
    int unsneakTicks;
    boolean sneaking;

    public SafeWalkHelper(ScaffoldModule module) {
        this.module = module;
    }

    public void update(Integer ground) {
        if (ground == null) {
            this.stopSneak();
            return;
        }
        ScaffoldSettings settings = this.module.settings;
        int delay = settings.earlySneak.getValue().intValue();       // Number.intValue (virtual)
        if (delay >= 1) {
            delay = jitter(delay);
        }
        if (!settings.isSafeWalk() || !settings.isSafeWalkSneak()) {
            this.stopSneak();
            return;
        }
        if (ground.intValue() + 1 > delay) {
            this.stopSneak();
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        BlockPos below = new BlockPos(player.posX, player.posY - 2.0D, player.posZ);
        Block block = mc.theWorld.getBlockState(below).getBlock();
        if (com.adjustclient.utilities.BlockUtil.canReplace(block, mc.theWorld, below)) {
            this.startSneak();
        }
    }

    /** Presses the sneak key and arms the release countdown ("Un-sneak delay" plus jitter). */
    void startSneak() {
        if (this.unsneakTicks == 0) {
            int delay = this.module.settings.unSneakDelay.getValue().intValue();
            this.unsneakTicks = delay < 1 ? 0 : jitter(delay);
        }
        mc.gameSettings.keyBindSneak.pressed = true;
        this.sneaking = true;
    }

    /** Counts the release delay down and, when it is over, gives the sneak key back to the player. */
    private void stopSneak() {
        if (!this.sneaking) {
            return;
        }
        if (this.unsneakTicks != 0) {
            this.unsneakTicks--;
            return;
        }
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        boolean down = GameSettings.isKeyDown(sneak);
        if (!down) {
            sneak.pressTime = 0;
        }
        sneak.pressed = down;
        this.sneaking = false;
    }

    @EventHandler
    public void onSafeWalk(SafeWalkEvent e) {
        if (this.module == null || !this.module.isEnabled() || mc.thePlayer == null || e.safeWalk) {
            return;
        }
        if ((this.module.noAscendActive ? 1 : 0) < mc.thePlayer.onGroundTicks) {
            ScaffoldSettings settings = this.module.settings;
            if (settings.isSafeWalk() && !settings.isSafeWalkSneak()) {
                e.safeWalk = true;
            }
        }
    }

    private static int jitter(int delay) {
        Random random = RandomUtil.RANDOM;
        if (random.nextBoolean()) {
            return delay + (random.nextBoolean() ? 1 : 0);
        }
        return delay;
    }
}
