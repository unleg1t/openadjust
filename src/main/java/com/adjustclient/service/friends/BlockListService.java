package com.adjustclient.service.friends;

import com.adjustclient.Adjust;
import com.adjustclient.event.Listener;
import com.adjustclient.service.BackendMessageHandler;
import com.adjustclient.user.ClientUser;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BlockListService implements Listener, BackendMessageHandler {
    public static BlockListService INSTANCE;
    public Set<Friend> friends = new HashSet<>();
    public boolean requested;
    public Object field12;

    public void onDisconnect() {
        for (Friend friend : friends.toArray(new Friend[0])) {
            friend.user.setFriend(false);
        }
    }

    public void onConnect() { requested = false; }

    public void sendBlockRequest() {
        requested = true;
    }

    @SuppressWarnings("unused")
    private void vendorSendBlockRequest() {
        if (requested) return;
        new com.adjustclient.backend.api.request.client.UpdateClientRequest().toPacket().sendAsync(null);
        requested = true;
    }

    @SuppressWarnings("unused")
    private static void vendorDecodeBlockList(
            Consumer<List<ClientUser>> callback,
            com.adjustclient.backend.api.response.BackendResponse response) {
        List<ClientUser> users = null;
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK
                && response.body instanceof com.adjustclient.backend.api.response.DataResponseBody) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                        ((com.adjustclient.backend.api.response.DataResponseBody) response.body).value));
                int size = in.readInt();
                users = new ArrayList<>(size);
                for (int i = 0; i < size; i++) users.add(Adjust.INSTANCE.field40.getOrCreate(in.readUTF()));
            } catch (Exception exception) {
                Logger.log(LogLevel.ERROR, true, "Failed to decode block list: {}", exception);
                users = null;
            }
        }
        callback.accept(users);
    }

    @SuppressWarnings("unused")
    private static Consumer<com.adjustclient.backend.api.response.BackendResponse> vendorBlockListCallback(
            Consumer<List<ClientUser>> callback) {
        return response -> vendorDecodeBlockList(callback, response);
    }
}
