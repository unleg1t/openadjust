package com.adjustclient.backend.transport;

import com.adjustclient.logging.Logger;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class HydraWriteHandler implements CompletionHandler<Integer, Object> {
    public final ByteBuffer buffer;
    public final HydraConnection connection;

    HydraWriteHandler(ByteBuffer buffer, HydraConnection connection) {
        this.buffer = buffer;
        this.connection = connection;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        try {
            if (this.buffer.hasRemaining()) {
                this.connection.channel.write(this.buffer, null, this);
                return;
            }
            ByteBuffer next = null;
            synchronized (this.connection.writeQueueLock) {
                this.connection.writeQueue.remove(0);
                if (!this.connection.writeQueue.isEmpty()) {
                    next = this.connection.writeQueue.get(0);
                }
            }
            if (next != null) {
                this.connection.channel.write(next, null, new HydraWriteHandler(next, this.connection));
            }
        } catch (Throwable exc) {
            Logger.debugStackTrace(exc, "Writing Hydra buffer");
            this.connection.checkTimeout();
        }
    }

    void a() {
        try {
            this.connection.channel.write(this.buffer, null, this);
        } catch (Throwable exc) {
            Logger.debugStackTrace(exc, "Writing Hydra buffer");
            this.connection.checkTimeout();
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        if (Logger.debug) {
            Logger.debugStackTrace(exc, "Writing Hydra buffer");
        }
        this.connection.checkTimeout();
    }
}
