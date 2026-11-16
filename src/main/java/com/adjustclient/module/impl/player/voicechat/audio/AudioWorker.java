package com.adjustclient.module.impl.player.voicechat.audio;

import com.adjustclient.event.Listener;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
import java.util.concurrent.*;

public abstract class AudioWorker implements Listener {
    public final ExecutorService executor;
    public final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    public final com.adjustclient.audio.device.DeviceType type;
    public volatile Future<?> loopFuture;
    public final VoiceAudioManager manager;

    protected AudioWorker(VoiceAudioManager manager) {
        this.manager = manager;
        this.type = this instanceof SpeakerWorker ? com.adjustclient.audio.device.DeviceType.OUTPUT : com.adjustclient.audio.device.DeviceType.INPUT;
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, this instanceof SpeakerWorker ? "Speaker" : "Microphone");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void requestClose() {
        tasks.add(() -> {
            try { closeDevice(); } catch (Exception ignored) { }
        });
    }

    public void closeDevice() {
        if (VoiceChatController.INSTANCE != null && VoiceChatModule.INSTANCE.isEnabled()) {
            VoiceChatController.INSTANCE.findAuthenticatedSender();
        }
    }

    public void tick() {
        Runnable task;
        while ((task = tasks.poll()) != null) task.run();
        processFrame();
    }

    public void runLoop() {
        try {
            synchronized (this) {
                // Ensure loopFuture assignment in start()/initialize() is visible before checking loop condition.
            }
            if (VoiceChatController.INSTANCE != null && VoiceChatModule.INSTANCE.isEnabled())
                VoiceChatController.INSTANCE.findAuthenticatedSender();
            while (loopFuture != null && !loopFuture.isDone() && !Thread.currentThread().isInterrupted()) {
                if (this instanceof MicrophoneWorker && !((MicrophoneWorker) this).isEncoderOpen()) break;
                long interval = this instanceof MicrophoneWorker
                        ? (manager.captureDevice == null ? 500L : 5L)
                        : (manager.outputDevice == null ? 500L : System.getProperty("org.graalvm.nativeimage.imagecode") == null ? 20L : 15L);
                long start = System.nanoTime();
                tick();
                long remaining = interval * 1000000L - (System.nanoTime() - start);
                // Thread.sleep supplies the JDK diagnostic "nanosecond timeout value out of range".
                if (remaining > 0L) Thread.sleep(remaining / 1000000L, (int) (remaining % 1000000L));
            }
            closeDevice();
        } catch (Exception exception) {
            exception.printStackTrace();
            closeDevice();
        }
    }

    public synchronized void start() {
        if (loopFuture != null && !loopFuture.isDone()) return;
        if (loopFuture != null) loopFuture.cancel(true);
        loopFuture = executor.submit(() -> runLoop());
    }
    public void enqueueTick() {
        tasks.add(() -> tick());
    }
    public abstract void processFrame();
}
