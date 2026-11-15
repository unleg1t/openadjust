package com.adjustclient.event;

import net.minecraft.client.Minecraft;

public final class PlayerKillEvent extends Event {
    public String victim;
    public String killer;
    public byte flags;
    public boolean finalKill;

    public PlayerKillEvent(String victim, String killer, boolean initialFlag, boolean finalKill) {
        this.victim = victim;
        this.killer = killer;
        this.flags = (byte) (initialFlag ? 1 : 0);
        this.finalKill = finalKill;
    }

    public boolean isLocalPlayer() {
        return killer != null && killer.equals(Minecraft.getMinecraft().getSession().getUsername());
    }
}
