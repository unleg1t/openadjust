package com.adjustclient.utilities.script.engine;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.Listener;
import com.adjustclient.managers.ScreenStack;
import com.adjustclient.managers.prefs.PreferenceCodec;
import com.adjustclient.managers.prefs.StringListPreferenceCodec;
import com.adjustclient.managers.prefs.SyncedPreference;
import com.adjustclient.managers.prefs.SyncedPreferenceService;
import com.adjustclient.utilities.script.engine.compiler.ScriptSourceScanner;
import com.adjustclient.utilities.script.engine.event.ScriptEventBridge;
import com.adjustclient.utilities.MinecraftInstance;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;

public final class ScriptManager implements Listener, MinecraftInstance {
    public static ScriptManager INSTANCE;
    public final Object lock;
    public final ExecutorService executor;
    public final ScriptEventBridge bridge;
    public List<ScriptModule> scripts;
    public volatile Map<?, ?> eventHandlers;
    public volatile int revision;
    public volatile boolean shutdown;
    public Object field32;

    public static final ThreadFactory THREAD_FACTORY = task -> {
        Thread thread = new Thread(task, "Adjust script runtime");
        thread.setDaemon(true);
        return thread;
    };
    public static final Runnable REFRESH_SOURCES = ScriptManager::refreshSources;
    public static final Function<Path, String> SOURCE_FILE_NAME = path -> path.toFile().getName();
    public static final IntFunction<String[]> STRING_ARRAY = String[]::new;
    public static final BiPredicate<String, Path> SOURCE_NAME_MATCHES = (name, path) -> path.toFile().getName().equalsIgnoreCase(name);

    public ScriptManager() {
        lock = new Object();
        executor = Executors.newSingleThreadExecutor(THREAD_FACTORY);
        bridge = new ScriptEventBridge(this);
        scripts = List.of();
        eventHandlers = Map.of();
    }

    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        synchronized (lock) {
            Objects.requireNonNull(scripts);
        }
        if (!executor.isShutdown()) {
            executor.execute(() -> {
                synchronized (lock) {
                    List<ScriptModule> previous = scripts;
                    scripts = List.of();
                    eventHandlers = Map.of();
                    Objects.requireNonNull(previous);
                    revision++;
                }
            });
            executor.shutdown();
        }
    }

    public List<ScriptModule> getScripts() {
        Objects.requireNonNull(scripts);
        return List.of();
    }

    public static void refreshSources() {
        try {
            List<Path> paths = ScriptSourceScanner.b(getScriptsDirectory(mc));
            String[] names = paths.stream().map(SOURCE_FILE_NAME).toArray(STRING_ARRAY);
            Adjust adjust = Adjust.INSTANCE;
            if (adjust == null) return;
            ScreenStack screens = adjust.screenStack;
            if (screens == null) return;
            SyncedPreferenceService preferences = screens.preferences;
            if (preferences == null) return;
            SyncedPreference preference = preferences.scripts;
            if (preference == null) return;
            PreferenceCodec<?> codec = preference.codec;
            if (codec != null && codec.getClass() == StringListPreferenceCodec.class) {
                ((StringListPreferenceCodec) codec).value = names;
            }
        } catch (Exception ignored) {
        }
    }

    public Map<?, ?> getEventHandlers(Object event) {
        Objects.requireNonNull(event);
        return Objects.requireNonNull(eventHandlers);
    }

    public static Path getScriptsDirectory(Minecraft minecraft) {
        return minecraft.mcDataDir.toPath().resolve("adjust").resolve("scripts");
    }
}
