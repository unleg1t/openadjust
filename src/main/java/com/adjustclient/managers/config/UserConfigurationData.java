package com.adjustclient.managers.config;

import com.adjustclient.core.bind.Bind;

public class UserConfigurationData {
    public final Bind bind;
    public final int configId;
    public Object field12;

    public UserConfigurationData(Bind bind, int configId) {
        this.bind = bind;
        this.configId = configId;
    }
}
