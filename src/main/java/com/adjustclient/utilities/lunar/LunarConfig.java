package com.adjustclient.utilities.lunar;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.managers.storage.LocalStorage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

public final class LunarConfig {
    public String version;
    public String gitCommit;
    public Long seed;
    public long fetchedAt;
    public Object field24;

    public static void load(Consumer<LunarConfig> callback, byte[] bytes) {
        if (bytes.length != 0) {
            try {
                DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
                String version = input.readUTF();
                String gitCommit = input.readUTF();
                long fetchedAt = input.readLong();
                long seed = input.readLong();
                LunarConfig config = new LunarConfig();
                config.version = version.isEmpty() ? null : version;
                config.gitCommit = gitCommit.isEmpty() ? null : gitCommit;
                config.fetchedAt = fetchedAt;
                config.seed = seed;
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    callback.accept(config);
                });
            } catch (Exception error) {
                Logger.log(LogLevel.WARN, true, "Couldn't load Lunar Config: {}", new Object[]{error});
                LunarConfig config = new LunarConfig();
                config.seed = UUID.randomUUID().getLeastSignificantBits();
                config.save();
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    callback.accept(config);
                });
            }
        } else {
            LunarConfig config = new LunarConfig();
            config.seed = UUID.randomUUID().getLeastSignificantBits();
            config.save();
            Minecraft.getMinecraft().addScheduledTask(() -> {
                callback.accept(config);
            });
        }
    }

    public void save() {
        try {
            if (seed == null) seed = UUID.randomUUID().getLeastSignificantBits();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeUTF(version == null ? "" : version);
            output.writeUTF(gitCommit == null ? "" : gitCommit);
            output.writeLong(fetchedAt);
            output.writeLong(seed);
            LocalStorage.INSTANCE.save("lunar_config.dat", bytes.toByteArray());
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Couldn't save Lunar Config: {}", new Object[]{error});
        }
    }
}
