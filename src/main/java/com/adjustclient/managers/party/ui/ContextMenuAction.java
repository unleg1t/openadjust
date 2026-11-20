package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
public class ContextMenuAction extends ContextMenuEntry {
    public TextComponent[] label;
    public Runnable action;
    public ContextMenuAction(TextComponent[] label, Runnable action) { this.label = label; this.action = action; }
}
