package com.adjustclient.module.impl.player.voicechat.client.user;

public class ModeratorNote {
    public final String note;
    public final String createdByName;
    public ModeratorNote(int id, String note, String createdByName, long updatedAt) {
        this.note = note;
        this.createdByName = createdByName;
    }
}
