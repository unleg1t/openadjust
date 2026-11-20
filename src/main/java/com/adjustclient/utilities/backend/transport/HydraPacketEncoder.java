package com.adjustclient.utilities.backend.transport;

import com.adjustclient.utilities.backend.hwid.HardwareId;
import com.adjustclient.utilities.backend.hwid.HwidPacketWriter;
import com.adjustclient.managers.prefs.SyncedPreferenceValue;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public final class HydraPacketEncoder {
    public static byte[] encodePreferences(SyncedPreferenceValue[] values) {
        int size = 5;
        for (SyncedPreferenceValue value : values) size += value.data.length + 3;
        byte[] packet = new byte[size];
        packet[0] = 40;
        packet[4] = (byte) values.length;
        int offset = 5;
        for (SyncedPreferenceValue value : values) {
            packet[offset] = (byte) value.id;
            byte[] data = value.data;
            if (data.length >= 65535) throw new RuntimeException("L[39]:" + data.length);
            packet[offset + 1] = (byte) (data.length >>> 8);
            packet[offset + 2] = (byte) data.length;
            offset += 3;
            for (int i = 0; i < data.length; i++) packet[offset + i] = data[i];
            offset += data.length;
        }
        if (size != offset) throw new RuntimeException("L[38]:" + offset + "," + size + ",C=" + values.length);
        return packet;
    }

    public static byte[] encodeLogin(int protocol, String hardwareId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[4], 0, 4);
            DataOutputStream out = new DataOutputStream(baos);
            HwidPacketWriter.a(1, hardwareId, out);
            byte[] packet = baos.toByteArray();
            packet[0] = 43;
            return packet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encodeUserLookup(int requestId, String name) {
        byte[] packet = new byte[10 + name.length() * 2];
        packet[0] = 7;
        packet[4] = (byte) (requestId >>> 24);
        packet[5] = (byte) (requestId >>> 16);
        packet[6] = (byte) (requestId >>> 8);
        packet[7] = (byte) requestId;
        char[] chars = name.toCharArray();
        if (chars.length >= 65536) throw new NullPointerException();
        packet[8] = (byte) (chars.length >>> 8);
        packet[9] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            packet[10 + i * 2] = (byte) (chars[i] >>> 8);
            packet[11 + i * 2] = (byte) chars[i];
        }
        return packet;
    }

    public static byte[] encodeDispatcherReply(String sender, boolean accepted) {
        byte[] packet = new byte[7 + sender.length() * 2];
        packet[0] = 29;
        packet[4] = (byte) (accepted ? 1 : 0);
        char[] chars = sender.toCharArray();
        if (chars.length >= 65536) throw new NullPointerException();
        packet[5] = (byte) (chars.length >>> 8);
        packet[6] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            packet[7 + i * 2] = (byte) (chars[i] >>> 8);
            packet[8 + i * 2] = (byte) chars[i];
        }
        return packet;
    }

    public static byte[] encodePartyInvite(String name, int partyId) {
        byte[] packet = new byte[10 + name.length() * 2];
        packet[0] = 16;
        char[] chars = name.toCharArray();
        if (chars.length >= 65536) throw new NullPointerException();
        packet[4] = (byte) (chars.length >>> 8);
        packet[5] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            packet[6 + i * 2] = (byte) (chars[i] >>> 8);
            packet[7 + i * 2] = (byte) chars[i];
        }
        int offset = 6 + name.length() * 2;
        packet[offset] = (byte) (partyId >>> 24);
        packet[offset + 1] = (byte) (partyId >>> 16);
        packet[offset + 2] = (byte) (partyId >>> 8);
        packet[offset + 3] = (byte) partyId;
        return packet;
    }

    public static byte[] encodeSendConfiguration(int requestId, int configurationId, byte[] data) {
        byte[] packet = new byte[data.length + 16];
        packet[0] = 21;
        packet[4] = (byte) (requestId >>> 24);
        packet[5] = (byte) (requestId >>> 16);
        packet[6] = (byte) (requestId >>> 8);
        packet[7] = (byte) requestId;
        packet[8] = (byte) (configurationId >>> 24);
        packet[9] = (byte) (configurationId >>> 16);
        packet[10] = (byte) (configurationId >>> 8);
        packet[11] = (byte) configurationId;
        packet[12] = (byte) (data.length >>> 24);
        packet[13] = (byte) (data.length >>> 16);
        packet[14] = (byte) (data.length >>> 8);
        packet[15] = (byte) data.length;
        for (int i = 0; i < data.length; i++) packet[16 + i] = data[i];
        return packet;
    }

    public static byte[] encodeRecoverAccount(long accountId, int mode, String hardwareId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[20], 0, 20);
            DataOutputStream out = new DataOutputStream(baos);
            HwidPacketWriter.a(1, hardwareId, out);
            byte[] packet = baos.toByteArray();
            packet[0] = 5;
            ByteCodec.writeLong(packet, 4, accountId);
            if (!HardwareId.initialized) {
                HardwareId.initialized = true;
                HardwareId.computeHardwareId();
            }
            ByteCodec.writeLong(packet, 12, HardwareId.cachedHash ^ 0x20L);
            return packet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encodeRegister(String username, String password, long hardwareIdHash) {
        byte[] packet = new byte[username.length() * 2 + password.length() * 2 + 16];
        packet[0] = 1;
        char[] uChars = username.toCharArray();
        if (uChars.length >= 65536) throw new NullPointerException();
        packet[4] = (byte) (uChars.length >>> 8);
        packet[5] = (byte) uChars.length;
        for (int i = 0; i < uChars.length; i++) {
            packet[6 + i * 2] = (byte) (uChars[i] >>> 8);
            packet[7 + i * 2] = (byte) uChars[i];
        }
        int offset = uChars.length * 2 + 6;
        char[] pChars = password.toCharArray();
        if (pChars.length >= 65536) throw new NullPointerException();
        packet[offset] = (byte) (pChars.length >>> 8);
        packet[offset + 1] = (byte) pChars.length;
        for (int i = 0; i < pChars.length; i++) {
            packet[offset + 2 + i * 2] = (byte) (pChars[i] >>> 8);
            packet[offset + 3 + i * 2] = (byte) pChars[i];
        }
        ByteCodec.writeLong(packet, packet.length - 8, hardwareIdHash);
        return packet;
    }

    public static byte[] encodeScriptUpload(byte transferId, String name, boolean compressed, byte[] data) {
        byte[] packet = new byte[data.length + 12 + name.length() * 2];
        packet[0] = 45;
        packet[4] = transferId;
        packet[5] = (byte) (compressed ? 1 : 0);
        packet[6] = (byte) (data.length >>> 24);
        packet[7] = (byte) (data.length >>> 16);
        packet[8] = (byte) (data.length >>> 8);
        packet[9] = (byte) data.length;
        for (int i = 0; i < data.length; i++) packet[10 + i] = data[i];
        char[] chars = name.toCharArray();
        if (chars.length >= 65536) throw new NullPointerException();
        int offset = data.length + 10;
        packet[offset] = (byte) (chars.length >>> 8);
        packet[offset + 1] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            packet[offset + 2 + i * 2] = (byte) (chars[i] >>> 8);
            packet[offset + 3 + i * 2] = (byte) chars[i];
        }
        return packet;
    }

    public static byte[] encodeAuthResponse(int length, int firstResponse, int secondResponse, byte[] data) {
        byte[] packet = new byte[data.length + 16];
        packet[0] = 19;
        packet[4] = (byte) (length >>> 24);
        packet[5] = (byte) (length >>> 16);
        packet[6] = (byte) (length >>> 8);
        packet[7] = (byte) length;
        packet[8] = (byte) (firstResponse >>> 24);
        packet[9] = (byte) (firstResponse >>> 16);
        packet[10] = (byte) (firstResponse >>> 8);
        packet[11] = (byte) firstResponse;
        packet[12] = (byte) (secondResponse >>> 24);
        packet[13] = (byte) (secondResponse >>> 16);
        packet[14] = (byte) (secondResponse >>> 8);
        packet[15] = (byte) secondResponse;
        for (int i = 0; i < length; i++) packet[16 + i] = data[i];
        return packet;
    }

    public static byte[] encodeMigrateHwid(int accountId, long oldHardwareIdHash, long newHardwareIdHash, int mode, String hardwareId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[22], 0, 22);
            DataOutputStream out = new DataOutputStream(baos);
            HwidPacketWriter.a(1, hardwareId, out);
            byte[] packet = baos.toByteArray();
            packet[0] = 44;
            ByteCodec.writeLong(packet, 4, oldHardwareIdHash);
            ByteCodec.writeLong(packet, 12, newHardwareIdHash);
            packet[20] = (byte) (accountId >>> 8);
            packet[21] = (byte) accountId;
            return packet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encodeKickPartyMember(String name) {
        byte[] packet = new byte[6 + name.length() * 2];
        packet[0] = 39;
        char[] chars = name.toCharArray();
        if (chars.length >= 65536) throw new NullPointerException();
        packet[4] = (byte) (chars.length >>> 8);
        packet[5] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            packet[6 + i * 2] = (byte) (chars[i] >>> 8);
            packet[7 + i * 2] = (byte) chars[i];
        }
        return packet;
    }

    public static byte[] encodeConfigurationDataRequest(int firstId, int secondId) {
        byte[] packet = new byte[12];
        packet[0] = 23;
        packet[4] = (byte) (firstId >>> 24);
        packet[5] = (byte) (firstId >>> 16);
        packet[6] = (byte) (firstId >>> 8);
        packet[7] = (byte) firstId;
        packet[8] = (byte) (secondId >>> 24);
        packet[9] = (byte) (secondId >>> 16);
        packet[10] = (byte) (secondId >>> 8);
        packet[11] = (byte) secondId;
        return packet;
    }

    public static byte[] encodePlayerReport(List<String> names) {
        int size = 8;
        for (String name : names) size += name.length() * 2 + 2;
        byte[] packet = new byte[size];
        packet[0] = 26;
        int count = names.size();
        packet[4] = (byte) (count >>> 24);
        packet[5] = (byte) (count >>> 16);
        packet[6] = (byte) (count >>> 8);
        packet[7] = (byte) count;
        int offset = 8;
        for (String name : names) {
            char[] chars = name.toCharArray();
            if (chars.length >= 65536) throw new NullPointerException();
            packet[offset] = (byte) (chars.length >>> 8);
            packet[offset + 1] = (byte) chars.length;
            for (int i = 0; i < chars.length; i++) {
                packet[offset + 2 + i * 2] = (byte) (chars[i] >>> 8);
                packet[offset + 3 + i * 2] = (byte) chars[i];
            }
            offset += name.length() * 2 + 2;
        }
        return packet;
    }

    public static byte[] encodeApiRequest(int endpointHash, long requestId, byte[] body) {
        byte[] packet = new byte[body.length + 20];
        packet[0] = 36;
        packet[4] = (byte) (endpointHash >>> 24);
        packet[5] = (byte) (endpointHash >>> 16);
        packet[6] = (byte) (endpointHash >>> 8);
        packet[7] = (byte) endpointHash;
        ByteCodec.writeLong(packet, 8, requestId);
        packet[16] = (byte) (body.length >>> 24);
        packet[17] = (byte) (body.length >>> 16);
        packet[18] = (byte) (body.length >>> 8);
        packet[19] = (byte) body.length;
        System.arraycopy(body, 0, packet, 20, body.length);
        return packet;
    }
}
