package com.adjustclient.anticheat.feature;

import com.adjustclient.Adjust;
import com.adjustclient.module.Module;
import java.util.Objects;

public final class ModuleFeature extends Feature {
    public final Class<? extends Module> moduleClass;
    public Object field20;

    public ModuleFeature(Class<? extends Module> moduleClass, String name, boolean unrestricted) {
        super(name, unrestricted);
        this.moduleClass = moduleClass;
    }

    public Module getModule() {
        if (field12 == null) field12 = Objects.requireNonNull(Adjust.INSTANCE.moduleManager.byClass.get(moduleClass));
        return (Module) field12;
    }
}
