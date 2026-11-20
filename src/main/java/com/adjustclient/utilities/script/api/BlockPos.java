package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Entity;
import com.adjustclient.utilities.script.api.Vec3;

public final class BlockPos {
    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z) {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public BlockPos(double x, double y, double z) {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public BlockPos(Entity source) {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public BlockPos(Vec3 source) {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    private native int $script$read$x();

    private native int $script$read$y();

    private native int $script$read$z();
}

