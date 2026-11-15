package com.adjustclient.service.telemetry;

import com.adjustclient.Adjust;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.request.misc.UnidentifiedRequestGB;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.backend.transport.HydraConnection;
import com.adjustclient.util.Cooldown;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class TelemetryQueue {
    public static TelemetryQueue INSTANCE;

    public final Map<UUID, TelemetryWriter> writers = new HashMap<>();
    public final Cooldown flushCooldown = new Cooldown(25000L);

    public void flush() {
        if (!isReady()) {
            return;
        }
        List<TelemetryWriter> pending = drainWriters();
        for (TelemetryWriter writer : pending) {
            sendWriter(writer, null);
        }
    }

    public void flushBlocking() {
        if (Adjust.hydraConnection == null) {
            return;
        }
        List<TelemetryWriter> pending = drainWriters();
        if (pending.isEmpty()) {
            return;
        }
        List<CountDownLatch> latches = new ArrayList<>(pending.size());
        for (TelemetryWriter writer : pending) {
            CountDownLatch latch = new CountDownLatch(1);
            latches.add(latch);
            sendWriter(writer, latch);
        }
        long deadline = System.currentTimeMillis() + 3500L;
        for (CountDownLatch latch : latches) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0L) {
                break;
            }
            try {
                latch.await(remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }

    public static boolean isReady() {
        HydraConnection conn = Adjust.hydraConnection;
        return conn != null
                && conn.authenticated
                && conn.ready
                && (HydraConnection.creditsExpiry - System.currentTimeMillis()) >= 1L;
    }

    public List<TelemetryWriter> drainWriters() {
        List<TelemetryWriter> ready = null;
        synchronized (writers) {
            for (TelemetryWriter writer : writers.values()) {
                boolean hasRecords;
                synchronized (writer.records) {
                    hasRecords = !writer.records.isEmpty();
                }
                if (hasRecords) {
                    if (ready == null) {
                        ready = new ArrayList<>();
                    }
                    ready.add(writer);
                }
            }
        }
        return ready == null ? Collections.emptyList() : ready;
    }

    public void sendWriter(TelemetryWriter writer, CountDownLatch latch) {
        TelemetryBatch batch = writer.buildBatch();
        PendingRequest request = new UnidentifiedRequestGB(
                writer.uuid, writer.username, batch.data, batch.timestamp).toPacket();
        request.sendAsync((BackendResponse response) -> {
            if (response == null || response.status != ResponseStatus.OK) {
                synchronized (writer.records) {
                    writer.records.addAll(Arrays.asList(batch.records));
                }
            }
            if (latch != null) {
                latch.countDown();
            }
        });
    }

    public void enqueue(TelemetryRecord record) {
        if (record == null) {
            return;
        }
        Session session = Minecraft.getMinecraft().getSession();
        if (session == null || session.getProfile() == null) {
            return;
        }
        String username = session.getUsername();
        UUID uuid = session.getProfile().getId();
        if (username == null || uuid == null || !username.matches("^\\w{2,16}$")) {
            return;
        }
        TelemetryWriter writer;
        synchronized (writers) {
            writer = writers.computeIfAbsent(uuid, k -> new TelemetryWriter(k, username));
        }
        synchronized (flushCooldown) {
            flushCooldown.timer.reset();
            flushCooldown.durationMs = 25000L;
        }
        record.timestamp = System.currentTimeMillis();
        synchronized (writer.records) {
            writer.records.add(record);
        }
        writer.recordCount++;
    }

    public TelemetryWriter createProfile() {
        Session session = Minecraft.getMinecraft().getSession();
        if (session == null || session.getPlayerID() == null || session.getUsername() == null) {
            return null;
        }
        String dashed = session.getPlayerID().replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        UUID uuid = UUID.fromString(dashed);
        GameProfile profile = new GameProfile(uuid, session.getUsername());
        synchronized (writers) {
            return writers.get(profile.getId());
        }
    }
}
