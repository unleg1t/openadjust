package com.adjustclient.backend.transport;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class HydraReadHandler implements CompletionHandler<Integer, Object> {
    public final ByteBuffer buffer;
    public final Runnable callback;
    public final HydraConnection connection;

    HydraReadHandler(ByteBuffer buffer, Runnable callback, HydraConnection connection) {
        this.buffer = buffer;
        this.callback = callback;
        this.connection = connection;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        a(result, attachment);
    }

    private void a(Integer result, Object attachment) {
        if (result == null || result < 1) {
            if (Logger.debug) {
                Logger.log(LogLevel.ERROR, true, "Receiving Hydra buffer: {}", result);
            }
            this.connection.checkTimeout();
            return;
        }
        if (this.buffer.hasRemaining()) {
            this.connection.channel.read(this.buffer, null, this);
        } else {
            this.callback.run();
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        if (Logger.debug) {
            Logger.debugStackTrace(exc, "Receiving Hydra buffer");
        }
        this.connection.checkTimeout();
    }
}
