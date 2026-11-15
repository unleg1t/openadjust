package com.adjustclient.service.config;

import com.adjustclient.bind.Bind;

public class UserConfigurationData {
    public final Bind bind;
    public final int configId;
    public Object field12;

    public UserConfigurationData(Bind bind, int configId) {
        this.bind = bind;
        this.configId = configId;
    }
}
