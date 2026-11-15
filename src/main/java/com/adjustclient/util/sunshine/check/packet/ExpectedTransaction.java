package com.adjustclient.util.sunshine.check.packet;
public class ExpectedTransaction extends ExpectedReply {
    public final int windowId;
    public final int actionId;
    public final boolean accepted;
    public Object field16;
    public ExpectedTransaction(int windowId, int actionId, boolean accepted) {
        this.windowId = windowId; this.actionId = actionId; this.accepted = accepted;
    }
    @Override public boolean matches(ExpectedReply other) {
        if (!(other instanceof ExpectedTransaction)) return false;
        ExpectedTransaction t = (ExpectedTransaction) other;
        return windowId == t.windowId && actionId == t.actionId && accepted == t.accepted;
    }
    @Override public String toString() {
        return "Transaction{windowId=" + windowId + ", actionId=" + actionId + ", accepted=" + accepted + "}";
    }
}
