package com.adjustclient.core.gui.tabgui;

@FunctionalInterface
public interface NodeFactory {

    GuiNode createNode(com.adjustclient.core.gui.tabgui.component.ColorComponent owner, com.adjustclient.core.gui.tabgui.component.ColorChannel a3, float[] values);
}
