package com.adjustclient.modules.impl.player.voicechat.protocol.transport.reliable;

public final class PendingFrame {
    public final byte[][] segments;
    public final boolean[] acknowledged;
    public final short sequence;
    public final long createdAt;
    public long lastSentAt;

    public PendingFrame(short sequence, short segmentCount) {
        this(new byte[segmentCount][], new boolean[segmentCount], sequence, System.currentTimeMillis(), 0);
    }

    public PendingFrame(byte[][] segments, boolean[] acknowledged, short sequence, long createdAt, long lastSentAt) {
        this.segments = segments;
        this.acknowledged = acknowledged;
        this.sequence = sequence;
        this.createdAt = createdAt;
        this.lastSentAt = lastSentAt;
    }

    public void setSegment(int index, byte[] data, boolean acknowledged) {
        synchronized (segments) {
            segments[index] = data;
            this.acknowledged[index] = acknowledged;
        }
    }

    public boolean isComplete() {
        synchronized (segments) {
            for (boolean flag : acknowledged) {
                if (!flag) return false;
            }
            return true;
        }
    }

    public boolean shouldResend(int index) {
        synchronized (segments) {
            synchronized (segments) {
                if (acknowledged[index]) return false;
            }
            return System.currentTimeMillis() - lastSentAt > 1000;
        }
    }

    public void markSent() {
        synchronized (segments) {
            lastSentAt = System.currentTimeMillis();
        }
    }

    public boolean isExpired() {
        synchronized (segments) {
            return System.currentTimeMillis() - createdAt > 5000;
        }
    }

    public byte getPacketId() {
        if (segments.length == 0) return 0;
        synchronized (segments) {
            return segments[0] == null ? 0 : segments[0][0];
        }
    }

    public byte[] assemble() {
        synchronized (segments) {
            int length = 0;
            for (byte[] segment : segments) length += segment.length;
            byte[] result = new byte[length];
            int offset = 0;
            for (byte[] segment : segments) {
                System.arraycopy(segment, 0, result, offset, segment.length);
                offset += segment.length;
            }
            return result;
        }
    }

    public boolean isEmptyFrame() {
        return (short) segments.length == 1 && segments[0] != null && segments[0].length == 0;
    }
}
