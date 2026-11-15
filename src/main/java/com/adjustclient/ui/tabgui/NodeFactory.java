package com.adjustclient.ui.tabgui;

@FunctionalInterface
public interface NodeFactory {

    GuiNode createNode(com.adjustclient.ui.tabgui.component.ColorComponent owner, com.adjustclient.ui.tabgui.component.ColorChannel a3, float[] values);
}
