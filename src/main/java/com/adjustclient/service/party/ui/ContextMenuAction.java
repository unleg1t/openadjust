package com.adjustclient.service.party.ui;
import com.adjustclient.render.font.*;
public class ContextMenuAction extends ContextMenuEntry {
    public TextComponent[] label;
    public Runnable action;
    public ContextMenuAction(TextComponent[] label, Runnable action) { this.label = label; this.action = action; }
}
