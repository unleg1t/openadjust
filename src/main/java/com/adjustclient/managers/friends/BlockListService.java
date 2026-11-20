package com.adjustclient.managers.friends;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.Listener;
import com.adjustclient.managers.BackendMessageHandler;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
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
        new com.adjustclient.utilities.backend.api.request.client.UpdateClientRequest().toPacket().sendAsync(null);
        requested = true;
    }

    @SuppressWarnings("unused")
    private static void vendorDecodeBlockList(
            Consumer<List<ClientUser>> callback,
            com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        List<ClientUser> users = null;
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK
                && response.body instanceof com.adjustclient.utilities.backend.api.response.DataResponseBody) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                        ((com.adjustclient.utilities.backend.api.response.DataResponseBody) response.body).value));
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
    private static Consumer<com.adjustclient.utilities.backend.api.response.BackendResponse> vendorBlockListCallback(
            Consumer<List<ClientUser>> callback) {
        return response -> vendorDecodeBlockList(callback, response);
    }
}
