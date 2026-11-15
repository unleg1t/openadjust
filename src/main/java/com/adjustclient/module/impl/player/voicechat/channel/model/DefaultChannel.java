package com.adjustclient.module.impl.player.voicechat.channel.model;

import java.util.UUID;

public final class DefaultChannel extends Channel {
    public DefaultChannel() { super(new UUID(0L, 0L)); }

    @Override public String getName() { return "Default"; }

    @Override public boolean isOwner() { return false; }

    @Override public boolean isCustom() { return false; }
}
