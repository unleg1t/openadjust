package com.adjustclient.command.impl;
import com.adjustclient.tag.PlayerTag;
public class WhitelistCommand extends PlayerListCommand {
    public WhitelistCommand(String name, PlayerTag tag) {
        super(name, tag);
        this.field8 = "Adds a player to your attack whitelist, preventing them from being attacked by modules.";
        this.aliases = new String[] {"whitelist"};
    }
    @Override protected String getListName() { return "attack whitelist"; }
}
