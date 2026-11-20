package com.adjustclient.core.settings.property;

import com.adjustclient.Adjust;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.managers.user.ClientUser;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.Consumer;

public class UserManager extends Manager<ClientUser> {
    public ClientUser field16;
    public final Int2ObjectOpenHashMap<Consumer<ClientUser>> pending = new Int2ObjectOpenHashMap<>();
    public int nextRequestId;
    public Object field28;

    public boolean matches(ClientUser user, Object key) {
        return key instanceof String ? user.name.equalsIgnoreCase((String) key) : key == user;
    }

    public void lookup(String name, Consumer<ClientUser> consumer) {
        if (!ServiceUtil.isValidName(name) || name.length() > 16) {
            consumer.accept(null);
            return;
        }
        ClientUser user = get(name);
        if (user != null) {
            consumer.accept(user);
            return;
        }
        consumer.accept(null);
    }

    @SuppressWarnings("unused")
    private void vendorLookup(String name, Consumer<ClientUser> consumer) {
        if (!ServiceUtil.isValidName(name) || name.length() > 16) {
            consumer.accept(null);
            return;
        }
        ClientUser user = get(name);
        if (user != null) {
            consumer.accept(user);
            return;
        }
        pending.put(nextRequestId, consumer);
        ServiceUtil.sendToBackend(com.adjustclient.utilities.backend.transport.HydraPacketEncoder.encodeUserLookup(nextRequestId, name));
        ++nextRequestId;
    }

    public void handleLookup(int requestId, boolean found, String name) {
        ClientUser user = null;
        if (found) {
            user = get(name);
            if (user == null) {
                user = new ClientUser(name);
                add(user);
            }
            user.name = name;
        }
        Consumer<ClientUser> consumer = pending.get(requestId);
        if (consumer != null) consumer.accept(user);
        pending.remove(requestId);
    }

    public ClientUser getOrCreate(String name) {
        ClientUser user = get(name);
        if (user != null) return user;
        user = new ClientUser(name);
        add(user);
        return user;
    }
}
