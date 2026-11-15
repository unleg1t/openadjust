package com.adjustclient.ui.clickgui.setting;

record BedPreviewTransform(float scale, float yOffset, float rotation) {
    static final BedPreviewTransform IDENTITY = new BedPreviewTransform(1.0F, 0.0F, 0.0F);
    static final BedPreviewTransform ZERO = new BedPreviewTransform(0.0F, 0.0F, 0.0F);
}
