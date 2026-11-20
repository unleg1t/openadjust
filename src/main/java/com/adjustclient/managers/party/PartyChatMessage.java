package com.adjustclient.managers.party;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.font.AccentText;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.gui.dialog.ActionLabel;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.ErrorReport;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PartyChatMessage {
    public String title;
    public TextComponent[] text;
    public ClientUser sender;
    public Animation animation;
    public Animation introAnimation;
    public ActionLabel accept;
    public ActionLabel decline;
    public Consumer<Boolean> onDecision;
    public int requestId;
    public long expiresAt;
    public boolean dismissed;

    public PartyChatMessage(String title, TextComponent[] text, ClientUser sender, int requestId) {
        this.title = title;
        this.text = text;
        this.sender = sender;
        this.requestId = requestId;
    }

    public static TextComponent[] format(ErrorReport report) {
        int gray = new Color(185, 185, 185, 255).getRGB();
        String message = report.message;
        Object[] arguments = report.arguments;
        List<TextComponent> components = new ArrayList<>();
        StringBuilder literal = new StringBuilder();
        int argumentIndex = 0;
        for (int i = 0; i < message.length(); i++) {
            char character = message.charAt(i);
            if (character == '{' && i + 1 < message.length() && message.charAt(i + 1) == '}') {
                if (literal.length() != 0) {
                    components.add(new TextComponent(literal.toString(), gray));
                    literal.setLength(0);
                }
                if (argumentIndex < arguments.length) {
                    components.add(new AccentText(String.valueOf(arguments[argumentIndex++])));
                } else {
                    components.add(new TextComponent("{}", gray));
                }
                i++;
            } else {
                literal.append(character);
            }
        }
        if (literal.length() != 0) {
            components.add(new TextComponent(literal.toString(), gray));
        }
        return components.toArray(new TextComponent[0]);
    }
}
