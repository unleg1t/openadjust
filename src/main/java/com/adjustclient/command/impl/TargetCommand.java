package com.adjustclient.command.impl;
import com.adjustclient.tag.PlayerTag;
public final class TargetCommand extends PlayerListCommand {
    public TargetCommand(String name, PlayerTag tag) {
        super(name, tag);
        this.field8 = "Adds a player to your enemy list, prioritizing them in combat and showing special marking.";
        this.aliases = new String[] {"target", "enemy", "tag"};
    }
    @Override protected String getListName() { return "target list"; }
}
