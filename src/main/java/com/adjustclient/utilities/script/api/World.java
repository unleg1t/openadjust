package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Block;
import com.adjustclient.utilities.script.api.Entity;
import com.adjustclient.utilities.script.api.Vec3;
import java.util.List;

public final class World {
    public native Block getBlockAt(Vec3 var1);

    public native String getDimensionName();

    public native List<Entity> getEntities();

    public native Entity getEntityById(int var1);
}

