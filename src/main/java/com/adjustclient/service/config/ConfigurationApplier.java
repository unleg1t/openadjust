package com.adjustclient.service.config;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindMode;
import com.adjustclient.bind.BindKey;
import com.adjustclient.property.KeybindManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class ConfigurationApplier {
    public final ArrayList<UserConfigurationData> binds = new ArrayList<>();
    public Object field8;
    public static ConfigurationManager queued;

    public void refresh(List<ConfigurationManager> configs) {
        ArrayList<UserConfigurationData> stale = new ArrayList<>(binds);
        for (ConfigurationManager config : configs) {
            UserConfigurationData data = find(config.id);
            if (data != null) {
                data.bind.name = config.name;
                if (!data.bind.keys.isEmpty()) stale.remove(data);
            }
        }
        for (UserConfigurationData data : stale) {
            if (!binds.contains(data)) continue;
            Adjust.INSTANCE.bindManager.objects.remove(data.bind);
            Adjust.INSTANCE.bindManager.notifyListeners();
            binds.remove(data);
        }
    }

    public void load(int id) {
        if (queued != null) return;
        ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        if (service == null) return;
        queued = service.find(id);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (queued != null) {
                queued.load();
                queued = null;
            }
        });
    }

    public UserConfigurationData find(int id) {
        return binds.stream().filter(data -> data.configId == id)
                .findFirst().orElse(null);
    }

    public UserConfigurationData getOrCreate(int id) {
        UserConfigurationData existing = find(id);
        if (existing != null) return existing;
        KeybindManager manager = Adjust.INSTANCE.bindManager;
        String bindId = "bind.configs.id" + id;
        int hash = KeybindManager.hashId(bindId);
        manager.checkCollision(bindId, hash, null);
        Bind bind = new Bind(hash, mode -> {
            if (mode == BindMode.PRESSED) load(id);
        }, null);
        manager.add(bind);
        bind.field29 = false;
        bind.hidden = true;
        UserConfigurationData data = new UserConfigurationData(bind, id);
        binds.add(data);
        ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        ConfigurationManager config = service == null ? null : service.find(id);
        if (config != null) bind.name = config.name;
        return data;
    }
}
