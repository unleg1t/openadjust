package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class ScaffoldMode implements Listener, MinecraftInstance, ModeOption {

    public ScaffoldModule module;
    protected String name;
    protected int capabilities;

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * True while Scaffold is enabled and this mode is the selected one (the mode's event handlers only act then).
     */
    public boolean isActive() {
        ScaffoldModule module = this.module;
        if (!module.isEnabled()) {
            return false;
        }
        return module.settings.mode.getValue() == this;
    }

    /**
     * Whether placing at height {@code y} is impossible because it is above the build limit of the world / minigame.
     * Normal and NCP use the world height (256), Hypixel uses the limit of the current Hypixel game.
     */
    public abstract boolean isAboveBuildLimit(int y);

    /**
     * Called for every placement Scaffold is about to execute (see {@code ScaffoldModule.handlePlacement}).
     * Normal remembers whether the player is towering ({@code facing == UP}); Hypixel and NCP do their per-mode work in
     * their event handlers instead.
     */
    public abstract void onPlace(PlacementTarget placement);

    /**
     * Whether sprinting is allowed while Scaffold is active in this mode (Allow sprint addon).
     */
    public abstract boolean canSprint();

    public abstract boolean e();

}
