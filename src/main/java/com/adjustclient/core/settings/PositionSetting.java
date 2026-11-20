package com.adjustclient.core.settings;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.movable.AnchorArea;
import com.adjustclient.utilities.game.movable.MovableAnchor;
import com.adjustclient.utilities.game.movable.MovablePosition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class PositionSetting extends Setting<MovablePosition> {
    private Object field48;

    public PositionSetting(SettingContainer parent, String name, MovablePosition position) {
        this(parent, name, position, null);
    }
    public PositionSetting(SettingContainer parent, String name, MovablePosition position, BooleanSupplier hideIf) {
        super(parent, name, position, hideIf);
    }

    public void read(DataInputStream input) throws IOException {
        MovableAnchor anchor = MovableAnchor.values()[input.readUnsignedByte()];
        float x = input.readFloat();
        float y = input.readFloat();
        AnchorArea area = Arrays.stream(Adjust.movableManager.anchorAreas)
                .filter(candidate -> candidate.anchor == anchor)
                .findFirst().orElseThrow(NullPointerException::new);
        MovablePosition old = getValue();
        MovablePosition next = new MovablePosition(area, x, y);
        setValueSilently(next);
        notifyListeners(old, next);
        fireChanged();
    }

    public void write(DataOutputStream output) throws IOException {
        MovablePosition position = getValue();
        output.writeByte(position.area.anchor.ordinal());
        output.writeFloat(position.x);
        output.writeFloat(position.y);
    }

    @Override public boolean setValueSilently(MovablePosition value) {
        return value != null && super.setValueSilently(value);
    }
}
