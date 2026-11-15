package com.adjustclient.service.config;

import com.adjustclient.Adjust;
import com.adjustclient.property.KeybindManager;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.Timer;
import com.adjustclient.bind.Bind;
import com.adjustclient.vfs.LoadOptions;
import com.adjustclient.vfs.VirtualFileSystem;
import java.nio.ByteBuffer;
import java.util.HashSet;
import net.minecraft.client.Minecraft;

public class ConfigurationManager {
    public String name;
    public long created;
    public long lastUsed;
    public int id;
    public Object field28;
    public static Timer loadTimer;

    public UserConfigurationData getOrCreateBind() {
        return UserConfiguration.INSTANCE.configurationBinds.getOrCreate(id);
    }

    public void load() {
        if (loadTimer != null && System.currentTimeMillis() < loadTimer.lastMs + 5000L) return;
        ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        if (service.selected.id == id && VirtualFileSystem.INSTANCE.dirtyTimer == null) return;
        loadTimer = new Timer();
        service.requestData(this, (status, bytes) -> {
            loadTimer = null;
            if (status != 193435832) return;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                LoadOptions options = new LoadOptions();
                options.keybindManager = Adjust.INSTANCE.bindManager;
                options.modules = true;
                options.keybinds = true;
                options.properties = true;
                if (VirtualFileSystem.INSTANCE.load(bytes, options)) service.select(this);
            });
        });
    }
}
