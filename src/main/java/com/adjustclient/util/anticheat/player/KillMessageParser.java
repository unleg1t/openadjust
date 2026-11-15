package com.adjustclient.util.anticheat.player;

import net.minecraft.util.IChatComponent;

public abstract class KillMessageParser {
    public abstract KillEvent parse(IChatComponent message);
}
