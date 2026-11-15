package com.adjustclient.service.friends;

import com.adjustclient.user.ClientUser;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class FriendDisplay {
    public final ClientUser sender;
    public final String text;
    public final AtomicInteger color = new AtomicInteger();
    public final Animation fade;
    public Object field20;
    public TextComponent[][] lines;
    public boolean field28;
    public Object field32;

    public FriendDisplay(ClientUser sender, String text) {
        this.sender = sender;
        this.text = text;
        fade = new Animation(Easing.LINEAR, 8500L);
        fade.value = 236.0F;
    }

    public TextComponent[] formatLine(int line, String value) {
        if (line != 0) return new TextComponent[] { new ReferenceColorText(value, color) };
        int separator = value.indexOf('\0');
        String body = separator + 3 > value.length() ? "" : value.substring(separator + 3);
        String name = separator >= 1 ? value.substring(0, separator) : value;
        TextComponent senderText = sender.useClientColor
                ? new AccentText(name) : new UserColorText(name, sender);
        return new TextComponent[] { senderText, new TextComponent(": ", new Color(155, 155, 155).getRGB()),
                new ReferenceColorText(body, color) };
    }

    public void draw(float x, float y) {
        updateColor();
        for (TextComponent[] line : lines) {
            TextComponent.draw(line, Fonts.TAHOMA_10, x, y);
            y += 7.0F;
        }
    }

    public void wrap(int width) {
        updateColor();
        List<IChatComponent> wrapped = GuiUtilRenderComponents.splitText(
                new ChatComponentText(sender.name + "\0: " + text), width, Fonts.TAHOMA_10, false, false);
        List<TextComponent[]> result = new ArrayList<>(1);
        for (int i = 0; i < wrapped.size(); i++) {
            result.add(formatLine(i, wrapped.get(i).getUnformattedText()));
        }
        lines = result.toArray(new TextComponent[0][]);
    }

    public void updateColor() {
        int value = (int) fade.value;
        color.set(new Color(value, value, value).getRGB());
        fade.update(200.0F);
    }
}
