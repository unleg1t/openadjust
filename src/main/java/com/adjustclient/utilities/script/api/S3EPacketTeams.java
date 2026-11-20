package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;
import java.util.Collection;

public final class S3EPacketTeams
extends Packet {
    public String name;
    public String displayName;
    public String prefix;
    public String suffix;
    public String nameTagVisibility;
    public int colorIndex;
    public Collection<String> players;
    public int action;
    public int flags;

    public S3EPacketTeams() {
    }

    public S3EPacketTeams(String name, String displayName, String prefix, String suffix, String nameTagVisibility, int colorIndex, Collection<String> players, int action, int flags) {
    }

    private native String $script$read$name();

    private native void $script$write$name(String var1);

    private native String $script$read$displayName();

    private native void $script$write$displayName(String var1);

    private native String $script$read$prefix();

    private native void $script$write$prefix(String var1);

    private native String $script$read$suffix();

    private native void $script$write$suffix(String var1);

    private native String $script$read$nameTagVisibility();

    private native void $script$write$nameTagVisibility(String var1);

    private native int $script$read$colorIndex();

    private native void $script$write$colorIndex(int var1);

    private native Collection<String> $script$read$players();

    private native void $script$write$players(Collection<String> var1);

    private native int $script$read$action();

    private native void $script$write$action(int var1);

    private native int $script$read$flags();

    private native void $script$write$flags(int var1);
}

