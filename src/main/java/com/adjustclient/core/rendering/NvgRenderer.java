package com.adjustclient.core.rendering;

import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.hud.layout.Rect2f;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class NvgRenderer {
    public static final Rect2f EMPTY_BOUNDS = new Rect2f();
    public List<DrawCommand> commands;
    public Stack<PushScissorCommand> transforms;
    public int color = -1;
    public int insertionIndex = -1;

    public NvgRenderer color(int color) { this.color = color; return this; }

    public NvgRenderer() { this(new ArrayList<>(), new Stack<>(), -1L); }
    public NvgRenderer(List<DrawCommand> commands, Stack<PushScissorCommand> transforms, long flags) {
        this.commands = commands;
        this.transforms = transforms;
        this.color = (int) flags;
        this.insertionIndex = (int) (flags >> 32);
    }

    public NvgRenderer popClip() { add(new PopScissorCommand()); return this; }

    public void bounds(Point2f offset) {
        for (DrawCommand command : commands) {
            Rect2f box = command.getBounds();
            Point2f point = new Point2f();
            point.x = box.x + offset.x;
            point.y = box.y + offset.y;
            command.setPosition(point);
        }
    }

    public void flush(ScaledResolution context) {
        for (DrawCommand command : commands) command.draw(this, context);
    }

    public void add(DrawCommand command) {
        if (insertionIndex == -1) commands.add(command);
        else commands.add(takeInsertIndex(), command);
    }

    public NvgRenderer rect(float x, float y, float width, float height) {
        add(new RectCommand(x, y, width, height, color));
        return this;
    }

    public NvgRenderer text(NvgFontRenderer font, String text, float x, float y) {
        add(new TextCommand(font, text, x, y, color, true));
        return this;
    }

    public NvgRenderer strokeRect(boolean outside, float x, float y, float width, float height, float lineWidth) {
        add(new StrokeRectCommand(x, y, width, height, lineWidth, color, outside));
        return this;
    }

    public NvgRenderer pushEvenScale() { add(new PushEvenScaleCommand()); return this; }

    public NvgRenderer pushClip(float x, float y, float width, float height) {
        if (!transforms.isEmpty()) {
            PushScissorCommand previous = transforms.peek();
            if (previous.x + previous.width > x + width) previous.width = x + width - previous.x;
        }
        PushScissorCommand clip = new PushScissorCommand(x, y, width, height);
        add(clip);
        transforms.push(clip);
        return this;
    }

    public NvgRenderer popEvenScale() { add(new PopEvenScaleCommand()); return this; }

    public Rect2f getBounds() {
        if (commands.isEmpty()) return EMPTY_BOUNDS;
        float minX = (float) Integer.MAX_VALUE, minY = (float) Integer.MAX_VALUE;
        float maxX = (float) Integer.MIN_VALUE, maxY = (float) Integer.MIN_VALUE;
        for (DrawCommand command : commands) {
            if (command instanceof StateCommand) continue;
            Rect2f r = command.getBounds();
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }
        Rect2f result = new Rect2f();
        result.x = minX; result.y = minY; result.width = maxX - minX; result.height = maxY - minY;
        return result;
    }

    public float getWidth() {
        if (commands.isEmpty()) return 0.0F;
        float minX = (float) Integer.MAX_VALUE, maxX = (float) Integer.MIN_VALUE;
        for (DrawCommand command : commands) {
            if (command instanceof StateCommand) continue;
            Rect2f r = command.getBounds();
            minX = Math.min(minX, r.x);
            maxX = Math.max(maxX, r.x + r.width);
        }
        return maxX - minX;
    }

    public NvgRenderer clear() {
        commands.clear();
        transforms.clear();
        color = -1;
        insertionIndex = -1;
        return this;
    }

    public int takeInsertIndex() {
        int index = insertionIndex;
        insertionIndex = -1;
        int size = commands.size();
        if (size == 0) return 0;
        return index == -1 || size <= index ? size - 1 : index;
    }
}
