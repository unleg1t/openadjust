package com.adjustclient.managers.payload;

import com.adjustclient.managers.BackendMessageHandler;
import com.adjustclient.managers.user.ClientUser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import java.util.function.Supplier;

public class PayloadService implements BackendMessageHandler {
    public Object field4;
    public static Int2ObjectMap<Supplier<PayloadHandler>> handlers = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
    public static final Supplier<PayloadHandler> TARGET = () -> new SetTargetPayload();
    public static final Supplier<PayloadHandler> GTB_THEME = () -> new com.adjustclient.modules.impl.player.guessthebuild.GuessTheBuildThemePayload();
    public static final Supplier<PayloadHandler> PING = () -> new PartyPingPayload();
    public static final Supplier<PayloadHandler> PRESET = () -> new BedDefensePresetPayload();

    static {
        handlers.put(0, TARGET);
        handlers.put(1, GTB_THEME);
        handlers.put(2, PING);
        handlers.put(3, PRESET);
    }

    public void onDisconnect() { }

    public void handlePayload(ClientUser sender, int requestId, int type, byte[] bytes) {
        Supplier<PayloadHandler> factory = handlers.get(type);
        if (factory != null) {
            PayloadHandler payload = factory.get();
            if (payload != null) {
                payload.read(new PacketBuffer(Unpooled.wrappedBuffer(bytes)));
                payload.handle(sender);
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorHandlePayload(ClientUser sender, int requestId, int type, byte[] bytes) {
        Supplier<PayloadHandler> factory = handlers.get(type);
        boolean accepted = false;
        if (factory != null) {
            PayloadHandler payload = factory.get();
            if (payload != null) {
                payload.read(new PacketBuffer(Unpooled.wrappedBuffer(bytes)));
                accepted = payload.handle(sender);
            }
        }
        new com.adjustclient.utilities.backend.api.request.client.ReplyCustomPayloadRequest(requestId, accepted).toPacket().sendAsync(null);
    }

    public void onConnect() { }
}
