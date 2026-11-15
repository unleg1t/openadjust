package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import net.minecraft.util.ResourceLocation;

public final class DebugEpsteinSubCommand extends SubCommand {
    public Object field16;

    public static final ResourceLocation TEXTURE;
    public static boolean enabled;

    static { TEXTURE = new ResourceLocation("adjust", "textures/other/epstein.png"); }

    public DebugEpsteinSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        enabled ^= true;
        parent.sendMessage(parent.createMessage().format("Turned Jeffrey Epstein {}!", enabled ? "on" : "off"));
    }
}
