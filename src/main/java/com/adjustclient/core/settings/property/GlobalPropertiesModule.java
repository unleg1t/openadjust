package com.adjustclient.core.settings.property;

import com.adjustclient.utilities.game.movable.MovablePosition;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.PositionSetting;

public final class GlobalPropertiesModule extends Module {
    public Object field32;

    public GlobalPropertiesModule() {
        super("(Hidden) Global Properties", Category.EXPLOITS);
        setHidden(false);
        // PositionSettings are registered directly onto this container by MovableManager.registerMovable.
    }

    private static MovablePosition anchoredDefault(float x, float y) {
        com.adjustclient.utilities.game.movable.AnchorArea nearest = null;
        float distance = Float.MAX_VALUE;
        for (com.adjustclient.utilities.game.movable.AnchorArea area : com.adjustclient.Adjust.movableManager.anchorAreas) {
            float dx = x - (area.width / 2.0F + area.x);
            float dy = y - (area.height / 2.0F + area.y);
            float squared = dx * dx + dy * dy;
            if (distance > squared) { distance = squared; nearest = area; }
        }
        com.adjustclient.utilities.game.movable.MovableAnchor anchor = java.util.Objects.requireNonNull(nearest).anchor;
        if (anchor != com.adjustclient.utilities.game.movable.MovableAnchor.TOP_LEFT) {
            x = (x - nearest.x) / nearest.width;
            if (!anchor.absoluteY) y = (y - nearest.y) / nearest.height;
        }
        return new MovablePosition(nearest, x, y);
    }
}
