package com.adjustclient.property;

import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.tag.PlayerTag;
import com.adjustclient.tag.PlayerTagData;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.util.ByteUtil;
import com.adjustclient.util.HashUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TagManager {
    public final HashMap<String, PlayerTagData> tags = new HashMap<>();
    public Integer lastSavedHash;
    public Object field12;

    public int removeTag(PlayerTag tag) {
        List<PlayerTagData> matching = getByTag(tag);
        for (PlayerTagData entry : matching) remove(entry.name);
        return matching.size();
    }

    public void deserialize(byte[] data) {
        if (data.length == 0) return;
        lastSavedHash = HashUtil.hash(data);
        int loaded = 0;
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            int count = input.readInt();
            PlayerTag[] kinds = {PlayerTag.values()[0], PlayerTag.values()[1]};
            for (int i = 0; i < count; ++i) {
                String name = input.readUTF();
                int ordinal = input.readUnsignedByte();
                long timestamp = ByteUtil.readVarLong(input) + 1726484560000L;
                if (ordinal < 2 && name.matches("^\\w{2,16}$")) {
                    setLoadedTag(name, kinds[ordinal], timestamp);
                    ++loaded;
                }
            }
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to parse saved tags (failed after {} tags): {}", loaded, exception);
        }
    }

    public PlayerTagData setLoadedTag(String name, PlayerTag tag, long timestamp) {
        // The allocation/insertion path returns the new entry; replacement also installs a fresh object.
        PlayerTagData entry = new PlayerTagData(name, tag, timestamp);
        tags.put(name.toLowerCase(), entry);
        return entry;
    }

    public List<String> getSavedNames() {
        return tags.entrySet().stream().filter(entry -> {
            return entry.getValue().tag == PlayerTag.WHITELIST;
        }).map(entry -> {
            return entry.getKey();
        }).toList();
    }

    public List<PlayerTagData> getByTag(PlayerTag tag) {
        List<PlayerTagData> matching = new ArrayList<>();
        for (Map.Entry<String, PlayerTagData> entry : tags.entrySet()) {
            if (entry.getValue().tag == tag) matching.add(entry.getValue());
        }
        return matching;
    }

    public PlayerTagData getData(String name) {
        return tags.get(name.toLowerCase());
    }

    public PlayerTagData setTag(String name, PlayerTag tag) {
        return setLoadedTag(name, tag, System.currentTimeMillis());
    }

    private byte[] serializeTags() {
        try {
            ArrayList<PlayerTagData> saved = new ArrayList<>(tags.values());
            saved.removeIf(entry -> {
                return entry.field12;
            });
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeInt(saved.size());
            for (PlayerTagData entry : saved) {
                output.writeUTF(entry.name);
                output.writeByte(entry.tag.ordinal());
                ByteUtil.writeVarLong(output, entry.time - 1726484560000L);
            }
            byte[] data = bytes.toByteArray();
            if (data.length > 3072) data = java.util.Arrays.copyOf(data, 3072);
            return data;
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to serialize tags: {}", exception);
            return null;
        }
    }

    /** Loads persisted player tags from {@link LocalStorage} when present. */
    public void loadLocal() {
        if (LocalStorage.INSTANCE == null) return;
        byte[] data = LocalStorage.INSTANCE.readNow("tags.dat");
        if (data != null && data.length > 0) deserialize(data);
    }

    public void save() {
        if (tags.isEmpty()) {
            if (lastSavedHash != null && LocalStorage.INSTANCE != null) {
                LocalStorage.INSTANCE.deleteNow("tags.dat");
                lastSavedHash = null;
            }
            return;
        }
        byte[] data = serializeTags();
        if (data == null) return;
        int hash = HashUtil.hash(data);
        if (lastSavedHash != null && lastSavedHash.intValue() == hash) return;
        if (LocalStorage.INSTANCE != null) {
            LocalStorage.INSTANCE.saveNow("tags.dat", data);
            lastSavedHash = hash;
        }
    }

    @SuppressWarnings("unused")
    private void vendorSave() {
        if (tags.isEmpty()) return;
        byte[] data = serializeTags();
        if (data == null) return;
        if (lastSavedHash != null && lastSavedHash.intValue() == HashUtil.hash(data)) return;
        // The request constructor supplies /client/setTagData and its original 45000 ms timeout.
        com.adjustclient.backend.api.request.client.SetTagDataRequest request =
                new com.adjustclient.backend.api.request.client.SetTagDataRequest(data);
        com.adjustclient.backend.api.PendingRequest pending = request.toPacket();
        pending.callback = response -> {
            if (response.status != com.adjustclient.backend.api.response.ResponseStatus.OK) {
                Logger.log(LogLevel.ERROR, true, "Failed to save tags: {}", response);
            }
        };
        if (!pending.trySend()) {
            pending.complete(new com.adjustclient.backend.api.response.BackendResponse(pending,
                    com.adjustclient.backend.api.response.ResponseStatus.DISCONNECTED,
                    com.adjustclient.backend.api.response.EmptyResponseBody.INSTANCE));
        }
    }

    public void remove(String name) {
        tags.remove(name.toLowerCase());
    }

    public PlayerTag getTag(String playerName) {
        PlayerTagData data = tags.get(playerName.toLowerCase());
        return data == null ? null : data.tag;
    }
}
