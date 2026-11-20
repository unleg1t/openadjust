package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.utilities.WorldUtil;
import net.minecraft.entity.EntityLivingBase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import com.adjustclient.utilities.StringUtil;

public final class TrackedLocalPlayer extends TrackedLivingEntity {
    public String lookupName;
    public String lookupResult;
    public Object field60;
    public static final ExecutorService EXECUTOR;
    static { EXECUTOR = Executors.newFixedThreadPool(2); }
    public TrackedLocalPlayer(EntityLivingBase entity) { super(entity); }
    public void lookup(String name) {
        if (name == null || name.equals(lookupName)) return;
        if (name.isEmpty()) { lookupName = name; lookupResult = null; return; }
        if (!((Pattern) StringUtil.UUID_PATTERN).matcher(name).matches()) return;
        lookupName = name;
        lookupResult = "Loading...";
        EXECUTOR.execute(() -> {
            try {
                lookupResult = WorldUtil.lookupUsername(name);
            } catch (java.io.IOException ignored) {
                lookupResult = null;
            }
        });
    }
}
