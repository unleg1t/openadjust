package com.adjustclient.vfs;

import com.adjustclient.module.Module;
import java.util.Objects;

record FeatureToggle(Module moduleFeature, boolean toggled) {
    @Override public boolean equals(Object other) {
        return this == other || other instanceof FeatureToggle toggle
                && toggled == toggle.toggled && Objects.equals(moduleFeature, toggle.moduleFeature);
    }

    @Override public int hashCode() {
        return 31 * Objects.hashCode(moduleFeature) + Boolean.hashCode(toggled);
    }

    @Override public String toString() {
        return "g[moduleFeature=" + moduleFeature + ", toggled=" + toggled + "]";
    }
}
