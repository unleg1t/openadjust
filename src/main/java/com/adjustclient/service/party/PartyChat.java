package com.adjustclient.service.party;

import com.adjustclient.Adjust;
import com.adjustclient.event.ScreenKeyEvent;
import com.adjustclient.render.NvgRenderer;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.TextInput;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.service.ScreenStack;
import com.adjustclient.service.party.chat.QuickMessage;
import com.adjustclient.service.party.ui.PartyPanel;
import com.adjustclient.util.CommandCompleter;
import com.adjustclient.util.Timer;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;

public final class PartyChat {
    public PartyPanel party;
    public TextInput input;
    public com.adjustclient.render.animation.Animation typingHeight = new com.adjustclient.render.animation.Animation(
            com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 100L);
    public com.adjustclient.render.animation.Animation typingFade = new com.adjustclient.render.animation.Animation(
            com.adjustclient.render.animation.Easing.LINEAR, 150L);
    public Timer typingTimer;
    public com.adjustclient.render.animation.Animation scrollFade = new com.adjustclient.render.animation.Animation(
            com.adjustclient.render.animation.Easing.LINEAR, 180L);
    public int completionIndex;
    public int completionCursor = -1;
    public int completionDelta;
    public boolean field40;

    public PartyChat(PartyPanel party) {
        this.party = party;
        String prompt = "Press " + ChatColor.WHITE + "ENTER" + ChatColor.RESET + " to send a message";
        input = new TextInput(prompt, com.adjustclient.render.font.Fonts.TAHOMA_10, 500);
        input.transform = this::highlightQuickMessages;
    }

    public void closeChat(boolean focused) {
        if (!focused) {
            ScreenStack screens = Adjust.INSTANCE.screenStack;
            if (screens.overlays.contains(PartyHudScreen.INSTANCE)) {
                com.adjustclient.service.util.ServiceUtil.closeOverlay(ScreenStack.deferOverlayChanges,
                        () -> screens.closeOverlay(PartyHudScreen.INSTANCE));
            }
            screens.partyService.stopTyping();
        }
        input.focused = focused;
    }

    public void onKey(ScreenKeyEvent event) {
        event.cancelled = true;
        GuiTextField field = input.field;
        if (event.keyCode == org.lwjgl.input.Keyboard.KEY_RETURN) {
            submit();
            return;
        }
        String before = field.getText();
        if (event.keyCode == org.lwjgl.input.Keyboard.KEY_TAB) {
            if (completionCursor == -1) completionCursor = field.getCursorPosition();
            String original = before;
            if (completionDelta != 0) original = before.substring(0, completionCursor)
                    + before.substring(completionCursor + completionDelta);
            String head = original.substring(0, completionCursor);
            int start = head.lastIndexOf(' ') + 1;
            String word = head.substring(start);
            Set<String> matches = new LinkedHashSet<>();
            CommandCompleter.completeParty(party, word, matches, word.toLowerCase());
            String[] choices = matches.toArray(new String[0]);
            if (choices.length != 0) {
                int index = completionIndex++;
                if (index >= choices.length) { completionIndex = 0; index = 0; }
                String replacement = choices[index];
                field.setText(original.substring(0, start) + replacement + original.substring(completionCursor));
                field.setCursorPosition(completionCursor + replacement.length() - word.length());
                completionDelta = replacement.length() - word.length();
            }
        } else {
            completionIndex = 0;
            completionCursor = -1;
            completionDelta = 0;
            field.setFocused(true);
            field.textboxKeyTyped((char) event.key, event.keyCode);
            if (!before.equals(field.getText())) {
                typingTimer = new Timer();
                Adjust.INSTANCE.screenStack.partyService.startTyping();
            }
        }
    }

    public static String expandQuickMessages(String text) {
        for (QuickMessage message : QuickMessage.MESSAGES) {
            String token = "{" + message.name + "}";
            String replacement = null;
            int index;
            while ((index = text.indexOf(token)) != -1) {
                if (replacement == null) replacement = message.buildText();
                text = text.substring(0, index) + replacement + text.substring(index + token.length());
            }
        }
        return text;
    }

    public void draw(int color, ScaledResolution context, float x, float y, float height, float width,
                     float inputHeight, float mouseX, float mouseY) {
        float top = y + height - inputHeight;
        NvgContext.pushEvenScale();
        org.joml.Vector4f line = NvgContext.toPixels(x, top, x + width, top - 0.5F);
        NvgDraw.fillRect(color, line.x, line.y, line.z, line.w);
        NvgContext.popEvenScale();
        if (mouseX >= x && mouseX <= x + width && mouseY >= top && mouseY <= y + height)
            com.adjustclient.render.RenderUtil.cursor = 221186;
        input.setWidth((int) width);
        NvgRenderer renderer = new NvgRenderer();
        input.draw(renderer, x + 2.5F, top + 4.0F);
        renderer.flush(context);
        if (typingTimer != null && typingTimer.hasElapsed(2400L)) {
            Adjust.INSTANCE.screenStack.partyService.stopTyping();
            typingTimer = null;
        }
    }

    public String highlightQuickMessages(String text) {
        for (QuickMessage message : QuickMessage.MESSAGES) {
            String token = "{" + message.name + "}";
            text = text.replace(token, ChatColor.BLUE.toString() + token + ChatColor.RESET.toString());
        }
        return text;
    }

    public void submit() {
        completionIndex = 0;
        completionCursor = -1;
        completionDelta = 0;
        if (!input.focused) return;
        String text = input.field.getText().trim();
        PartyService service = Adjust.INSTANCE.screenStack.partyService;
        if (!text.isEmpty()) {
            if (!service.isMessageRateLimited()) {
                service.sendRaw(expandQuickMessages(text));
                input.field.setText("");
            }
        } else input.field.setText("");
        ScreenStack screens = Adjust.INSTANCE.screenStack;
        if (screens.overlays.contains(PartyHudScreen.INSTANCE)) {
            com.adjustclient.service.util.ServiceUtil.closeOverlay(ScreenStack.deferOverlayChanges,
                    () -> screens.closeOverlay(PartyHudScreen.INSTANCE));
        }
        service.stopTyping();
        input.focused = false;
    }
}
