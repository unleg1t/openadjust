package com.adjustclient.utilities.backend.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import com.adjustclient.utilities.backend.transport.ByteCodec;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.repository.AccountRepository;
import com.adjustclient.managers.config.ConfigurationManager;
import com.adjustclient.core.gui.config.GlobalConfigEntry;
import com.adjustclient.managers.user.ClientUser;

public abstract class BackendRequest {
    public List<Object> args;
    public long timeoutMs;
    public int endpointHash;
    private static long nextRequestId;

    protected static final String CREATE_GLOBAL_CONFIGURATION = "/configuration/createGlobalConfiguration";
    protected static final String REMOVE_GLOBAL_CONFIGURATION = "/configuration/removeGlobalConfiguration";
    protected static final String UPDATE_GLOBAL_CONFIGURATION = "/configuration/updateGlobalConfiguration";
    protected static final String INVITE_FRIENDS_REPOSITORY = "/friends/inviteRepository";
    protected static final String GET_MESSAGE_HISTORY = "/globalchat/getMessageHistory";
    protected static final String GET_INVALID_BLOCKS = "/hypixel/getInvalidBlocks";
    protected static final String CHECK_IP_BAN = "/repository/checkIpBan";
    protected static final String REQUEST_REPOSITORY_DATA = "/repository/requestRepositoryData";
    protected static final String KICK_REPOSITORY_USER = "/repository/kickUser";
    protected static final String SET_LAST_LOGIN = "/repository/setLastLogin";

    protected BackendRequest(String endpoint, long timeoutMs, List<Object> args) {
        this.args = args;
        this.timeoutMs = timeoutMs;
        this.endpointHash = ByteCodec.endpointHash(endpoint);
    }

    public PendingRequest toPacket() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        int index = -1;
        try {
            for (Object field : args) {
                ++index;
                writeField(field, out);
            }
            return new PendingRequest(this, bytes.toByteArray(), nextRequestId++, timeoutMs);
        } catch (Exception exception) {
            throw new RuntimeException("Writing field #" + index, exception);
        }
    }

    public static void writeField(Object value, DataOutputStream out) throws IOException {
        if (value instanceof AccountEntry) {
            AccountEntry account = (AccountEntry) value;
            out.writeInt(account.id);
            out.writeInt(account.repository.id);
        } else if (value instanceof ConfigurationManager) {
            out.writeInt(((ConfigurationManager) value).id);
        } else if (value instanceof GlobalConfigEntry) {
            out.writeInt(((GlobalConfigEntry) value).id);
        } else if (value instanceof ClientUser) {
            out.writeUTF(((ClientUser) value).name);
        } else if (value instanceof Boolean) out.writeBoolean((Boolean) value);
        else if (value instanceof String) out.writeUTF((String) value);
        else if (value instanceof Integer) out.writeInt((Integer) value);
        else if (value instanceof Byte) out.writeByte((Byte) value);
        else if (value instanceof Long) out.writeLong((Long) value);
        else if (value instanceof Enum<?>) out.writeShort(((Enum<?>) value).ordinal());
        else if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            out.writeInt(bytes.length);
            out.write(bytes);
        } else if (value instanceof UUID) {
            UUID uuid = (UUID) value;
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } else if (value instanceof NullableValue) {
            NullableValue nullable = (NullableValue) value;
            out.writeBoolean(nullable.value == null);
            if (nullable.value != null) {
                if (!(nullable.value instanceof UUID)) throw new IOException("Unknown type: " + nullable);
                UUID uuid = (UUID) nullable.value;
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        } else if (value instanceof AccountEntry[]) {
            AccountEntry[] accounts = (AccountEntry[]) value;
            out.writeInt(accounts.length);
            for (AccountEntry account : accounts) out.writeInt(account.id);
        } else if (value instanceof AccountRepository) {
            out.writeInt(((AccountRepository) value).id);
        } else {
            throw new IOException("Unknown field type: " + value);
        }
    }
}
