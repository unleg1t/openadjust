package net.minecraft.client.gui;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.render.tweaks.TweaksModule;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

public class CommandSuggestions
{
    private static final int MAX_VISIBLE_SUGGESTIONS = 10;
    private static final int ROW_HEIGHT = 12;

    private final GuiChat chatScreen;
    public final List<String> completionCandidates = Lists.newArrayList();
    public final List<String> suggestions = Lists.newArrayList();
    private String pendingPrefix = "";
    private String lastInput = "";
    public String lastCompletedInput = "";
    private int lastCursorPosition;
    private long lastInputChangeTime = System.currentTimeMillis();
    private long lastRequestTime;
    private int pendingCursorPosition;
    public int lastCompletedCursorPosition = -1;
    public int selectedSuggestionIndex;
    public int firstVisibleSuggestionIndex;
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    public boolean awaitingCompletionResponse;
    private boolean userInteracted = true;

    public CommandSuggestions(GuiChat chatScreen)
    {
        this.chatScreen = chatScreen;
    }

    private static boolean isModernChatSuggestionsEnabled()
    {
        return TweaksModule.INSTANCE != null
                && TweaksModule.INSTANCE.chat != null
                && TweaksModule.INSTANCE.chat.isChatEnabled();
    }

    private static boolean isMinecraftChannelSelected()
    {
        return Adjust.chatManager == null || Adjust.chatManager.selected == Adjust.chatManager.minecraftChannel;
    }

    public void resetSuggestionState()
    {
        this.completionCandidates.clear();
        this.suggestions.clear();
        this.awaitingCompletionResponse = false;
        this.lastCompletedInput = "";
        this.lastCompletedCursorPosition = -1;
        this.selectedSuggestionIndex = 0;
        this.firstVisibleSuggestionIndex = 0;
    }

    public void reset()
    {
        this.resetSuggestionState();
        this.lastInput = "";
        this.pendingPrefix = "";
        this.lastCursorPosition = 0;
        this.userInteracted = true;
    }

    public boolean isActive()
    {
        return !this.suggestions.isEmpty();
    }

    public void tick()
    {
        if (this.chatScreen.inputField == null)
        {
            return;
        }

        if (!isMinecraftChannelSelected())
        {
            this.resetSuggestionState();
            return;
        }

        String text = this.chatScreen.inputField.getText();
        int cursor = this.chatScreen.inputField.getCursorPosition();

        if (this.updateWordContext(text, cursor))
        {
            if (!isModernChatSuggestionsEnabled() || !this.userInteracted || !this.chatScreen.isCommandInput())
            {
                this.resetSuggestionState();
            }
        }

        if (this.awaitingCompletionResponse)
        {
            if (System.currentTimeMillis() - this.lastRequestTime < 1000L)
            {
                return;
            }
            this.awaitingCompletionResponse = false;
        }

        if (!isModernChatSuggestionsEnabled() || !this.userInteracted || !this.chatScreen.isCommandInput() || text.isEmpty())
        {
            return;
        }

        if ((!text.equals(this.lastCompletedInput) || cursor != this.lastCompletedCursorPosition)
                && System.currentTimeMillis() - this.lastInputChangeTime >= 100L)
        {
            this.requestCompletions(false);
        }
    }

    public void onInputChanged()
    {
        this.userInteracted = true;
        if (this.chatScreen.inputField == null)
        {
            return;
        }

        String text = this.chatScreen.inputField.getText();
        int cursor = this.chatScreen.inputField.getCursorPosition();

        if (!this.hasNonWhitespace(text) || !isMinecraftChannelSelected() || !isModernChatSuggestionsEnabled())
        {
            this.resetSuggestionState();
            this.lastInput = text;
            this.lastCursorPosition = cursor;
            this.lastInputChangeTime = System.currentTimeMillis();
            return;
        }

        if (!this.chatScreen.isCommandInput())
        {
            this.resetSuggestionState();
            return;
        }

        if (this.updateWordContext(text, cursor) && this.chatScreen.shouldAutoRequestCompletions())
        {
            this.requestCompletions(true);
        }
    }

    public boolean handleKey(int keyCode)
    {
        if (this.chatScreen.inputField == null)
        {
            return false;
        }

        if (!isModernChatSuggestionsEnabled())
        {
            String text = this.chatScreen.inputField.getText();
            int cursor = this.chatScreen.inputField.getCursorPosition();
            if (this.updateWordContext(text, cursor))
            {
                this.resetSuggestionState();
            }

            if (keyCode != Keyboard.KEY_TAB)
            {
                this.resetSuggestionState();
                return false;
            }

            if (!isMinecraftChannelSelected())
            {
                return false;
            }

            if (this.suggestions.isEmpty())
            {
                this.requestCompletions(false);
                return true;
            }

            this.applySuggestion(this.selectedSuggestionIndex);
            int next = this.selectedSuggestionIndex + 1;
            if (next >= this.suggestions.size())
            {
                next = 0;
            }
            this.selectIndex(next);
            return true;
        }

        if (keyCode == Keyboard.KEY_ESCAPE && !this.suggestions.isEmpty())
        {
            this.userInteracted = false;
            this.resetSuggestionState();
            return true;
        }

        if (keyCode == Keyboard.KEY_UP && !this.suggestions.isEmpty())
        {
            int next = this.selectedSuggestionIndex - 1;
            if (next < 0)
            {
                next = this.suggestions.size() - 1;
            }
            this.selectIndex(next);
            return true;
        }

        if (keyCode == Keyboard.KEY_DOWN && !this.suggestions.isEmpty())
        {
            int next = this.selectedSuggestionIndex + 1;
            if (next >= this.suggestions.size())
            {
                next = 0;
            }
            this.selectIndex(next);
            return true;
        }

        if (keyCode == Keyboard.KEY_TAB)
        {
            if (!isMinecraftChannelSelected())
            {
                return false;
            }

            if (this.suggestions.isEmpty())
            {
                this.requestCompletions(false);
                return true;
            }

            this.applySuggestion(this.selectedSuggestionIndex);
            int next = this.selectedSuggestionIndex + (GuiScreen.isShiftKeyDown() ? -1 : 1);
            if (next < 0)
            {
                next = this.suggestions.size() - 1;
            }
            else if (next >= this.suggestions.size())
            {
                next = 0;
            }
            this.selectIndex(next);
            return true;
        }

        return false;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (!isModernChatSuggestionsEnabled() || button != 0 || this.suggestions.isEmpty())
        {
            return false;
        }

        int hovered = this.getHoveredIndex(mouseX, mouseY);
        if (hovered >= 0 && hovered < this.suggestions.size())
        {
            this.selectIndex(hovered);
            this.applySuggestion(hovered);
            return true;
        }

        return false;
    }

    public boolean handleMouseWheel(int wheelDelta)
    {
        if (!isModernChatSuggestionsEnabled() || this.suggestions.size() <= MAX_VISIBLE_SUGGESTIONS)
        {
            return false;
        }

        int hovered = this.getHoveredIndex(this.lastMouseX, this.lastMouseY);
        if (hovered < 0)
        {
            return false;
        }

        int step = wheelDelta > 0 ? -1 : 1;
        int maxFirst = Math.max(0, this.suggestions.size() - MAX_VISIBLE_SUGGESTIONS);
        this.firstVisibleSuggestionIndex = MathHelper.clamp_int(this.firstVisibleSuggestionIndex + step, 0, maxFirst);
        return true;
    }

    public void onCompletionResponse(String[] candidates)
    {
        if (!this.awaitingCompletionResponse || this.chatScreen.inputField == null || !isMinecraftChannelSelected())
        {
            return;
        }

        if (!this.chatScreen.inputField.getText().equals(this.pendingPrefix))
        {
            return;
        }

        this.awaitingCompletionResponse = false;
        Set<String> uniqueCandidates = new LinkedHashSet<>();

        if (candidates != null)
        {
            for (String candidate : candidates)
            {
                if (candidate != null && !candidate.isEmpty())
                {
                    uniqueCandidates.add(candidate);
                }
            }
        }

        if (Adjust.chatTextReplacer != null)
        {
            int safeCursor = MathHelper.clamp_int(this.pendingCursorPosition, 0, this.pendingPrefix.length());
            List<String> emojiCompletions = Adjust.chatTextReplacer.getCompletions(this.pendingPrefix.substring(0, safeCursor));
            if (emojiCompletions != null)
            {
                for (String candidate : emojiCompletions)
                {
                    if (candidate != null && !candidate.isEmpty())
                    {
                        uniqueCandidates.add(candidate);
                    }
                }
            }
        }

        this.completionCandidates.clear();
        this.completionCandidates.addAll(uniqueCandidates);

        Collections.sort(this.completionCandidates, String.CASE_INSENSITIVE_ORDER);
        this.lastCompletedInput = this.pendingPrefix;
        this.lastCompletedCursorPosition = this.pendingCursorPosition;

        if (isModernChatSuggestionsEnabled())
        {
            this.filterSuggestions();
        }
        else
        {
            this.filterSuggestions();
            if (this.suggestions.size() > 1)
            {
                String commonPrefix = StringUtils.getCommonPrefix(this.suggestions.toArray(new String[0]));
                if (!commonPrefix.isEmpty())
                {
                    this.applyCommonPrefix(commonPrefix);
                }
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                    new ChatComponentText(String.join(", ", this.suggestions)), 1);
            }
            else if (this.suggestions.size() == 1)
            {
                this.applySuggestion(0);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY)
    {
        if (!isModernChatSuggestionsEnabled())
        {
            return;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        if (this.suggestions.isEmpty() || this.chatScreen.inputField == null)
        {
            return;
        }

        int visibleCount = Math.min(this.suggestions.size(), MAX_VISIBLE_SUGGESTIONS);
        int hovered = this.getHoveredIndex(mouseX, mouseY);
        if (hovered >= 0)
        {
            this.selectIndex(hovered);
        }

        int boxX = this.getBoxX();
        int boxWidth = this.getBoxWidth();
        int boxY = this.chatScreen.inputField.yPosition - visibleCount * ROW_HEIGHT;
        int boxTop = boxY - 3;
        int boxBottom = boxTop + visibleCount * ROW_HEIGHT;

        Gui.drawRect(boxX - 1, boxTop, boxX + boxWidth + 1, boxBottom, 0xD0000000);

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        for (int i = 0; i < visibleCount; i++)
        {
            int index = this.firstVisibleSuggestionIndex + i;
            if (index >= this.suggestions.size())
            {
                break;
            }

            String suggestion = this.suggestions.get(index);
            int y = boxY + i * ROW_HEIGHT;
            int color = (index == this.selectedSuggestionIndex) ? 0xFFFFFF00 : 0xFFAAAAAA;
            font.drawStringWithShadow(suggestion, (float) (boxX + 1), (float) y, color);
        }
    }

    private boolean updateWordContext(String text, int cursor)
    {
        if (text.equals(this.lastInput) && cursor == this.lastCursorPosition)
        {
            return false;
        }

        this.lastInput = text;
        this.lastCursorPosition = cursor;
        this.lastInputChangeTime = System.currentTimeMillis();
        return true;
    }

    private void requestCompletions(boolean force)
    {
        if (this.chatScreen.inputField == null)
        {
            return;
        }

        String text = this.chatScreen.inputField.getText();
        if (!this.hasNonWhitespace(text) || (!force && this.awaitingCompletionResponse))
        {
            return;
        }

        int cursor = MathHelper.clamp_int(this.chatScreen.inputField.getCursorPosition(), 0, text.length());
        int wordStart = getWordStart(text, cursor);
        String prefix = text.substring(0, cursor);
        String wordPrefix = text.substring(wordStart, cursor).toLowerCase(Locale.ROOT);

        this.pendingPrefix = text;
        this.pendingCursorPosition = cursor;
        this.lastRequestTime = System.currentTimeMillis();
        this.awaitingCompletionResponse = true;

        if (!this.chatScreen.tryLocalAutocomplete(prefix, wordPrefix))
        {
            this.awaitingCompletionResponse = false;
            this.lastCompletedInput = text;
            this.lastCompletedCursorPosition = cursor;
        }
    }

    private void filterSuggestions()
    {
        if (this.chatScreen.inputField == null)
        {
            return;
        }

        String text = this.chatScreen.inputField.getText();
        int cursor = MathHelper.clamp_int(this.chatScreen.inputField.getCursorPosition(), 0, text.length());
        int wordStart = getWordStart(text, cursor);
        String token = text.substring(wordStart, cursor).toLowerCase(Locale.ROOT);

        this.suggestions.clear();
        for (String candidate : this.completionCandidates)
        {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(token))
            {
                this.suggestions.add(candidate);
            }
        }

        if (this.suggestions.isEmpty())
        {
            this.selectedSuggestionIndex = 0;
            this.firstVisibleSuggestionIndex = 0;
        }
        else
        {
            this.selectIndex(MathHelper.clamp_int(this.selectedSuggestionIndex, 0, this.suggestions.size() - 1));
        }
    }

    private void selectIndex(int index)
    {
        if (this.suggestions.isEmpty())
        {
            this.selectedSuggestionIndex = 0;
            this.firstVisibleSuggestionIndex = 0;
            return;
        }

        this.selectedSuggestionIndex = MathHelper.clamp_int(index, 0, this.suggestions.size() - 1);
        if (this.selectedSuggestionIndex < this.firstVisibleSuggestionIndex)
        {
            this.firstVisibleSuggestionIndex = this.selectedSuggestionIndex;
        }
        else if (this.selectedSuggestionIndex >= this.firstVisibleSuggestionIndex + MAX_VISIBLE_SUGGESTIONS)
        {
            this.firstVisibleSuggestionIndex = this.selectedSuggestionIndex - MAX_VISIBLE_SUGGESTIONS + 1;
        }
    }

    private void applySuggestion(int index)
    {
        if (index < 0 || index >= this.suggestions.size() || this.chatScreen.inputField == null)
        {
            return;
        }

        String chosen = this.suggestions.get(index);
        String text = this.chatScreen.inputField.getText();
        int cursor = MathHelper.clamp_int(this.chatScreen.inputField.getCursorPosition(), 0, text.length());
        int wordStart = getWordStart(text, cursor);

        String updated = text.substring(0, wordStart) + chosen + text.substring(cursor);
        this.chatScreen.inputField.setText(updated);
        this.chatScreen.inputField.setCursorPosition(wordStart + chosen.length());
        this.lastInput = updated;
        this.lastCursorPosition = this.chatScreen.inputField.getCursorPosition();
    }

    private void applyCommonPrefix(String commonPrefix)
    {
        if (this.chatScreen.inputField == null || commonPrefix.isEmpty())
        {
            return;
        }

        String text = this.chatScreen.inputField.getText();
        int cursor = MathHelper.clamp_int(this.chatScreen.inputField.getCursorPosition(), 0, text.length());
        int wordStart = getWordStart(text, cursor);
        String current = text.substring(wordStart, cursor);
        if (!current.equalsIgnoreCase(commonPrefix) && commonPrefix.length() > current.length())
        {
            String updated = text.substring(0, wordStart) + commonPrefix + text.substring(cursor);
            this.chatScreen.inputField.setText(updated);
            this.chatScreen.inputField.setCursorPosition(wordStart + commonPrefix.length());
        }
    }

    private int getHoveredIndex(int mouseX, int mouseY)
    {
        if (this.suggestions.isEmpty() || this.chatScreen.inputField == null)
        {
            return -1;
        }

        int visibleCount = Math.min(this.suggestions.size(), MAX_VISIBLE_SUGGESTIONS);
        int boxX = this.getBoxX();
        int boxWidth = this.getBoxWidth();
        int boxTop = this.chatScreen.inputField.yPosition - visibleCount * ROW_HEIGHT - 3;
        int boxBottom = boxTop + visibleCount * ROW_HEIGHT;

        if (mouseX < boxX - 1 || mouseX > boxX + boxWidth + 1 || mouseY < boxTop || mouseY > boxBottom)
        {
            return -1;
        }

        int row = (mouseY - boxTop) / ROW_HEIGHT;
        int index = this.firstVisibleSuggestionIndex + row;
        return (index >= 0 && index < this.suggestions.size()) ? index : -1;
    }

    private int getBoxX()
    {
        GuiTextField field = this.chatScreen.inputField;
        String text = field.getText();
        int cursor = MathHelper.clamp_int(field.getCursorPosition(), 0, text.length());
        int wordStart = getWordStart(text, cursor);
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int offset = (int)font.getStringWidth(text.substring(0, wordStart));
        int width = this.getBoxWidth();
        int maxRight = field.xPosition + field.width - width - 2;
        return Math.max(field.xPosition, Math.min(field.xPosition + offset, maxRight));
    }

    private int getBoxWidth()
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int maxWidth = 0;
        for (String suggestion : this.suggestions)
        {
            maxWidth = Math.max(maxWidth, (int)font.getStringWidth(suggestion));
        }
        return Math.max(maxWidth + 4, 20);
    }

    private boolean hasNonWhitespace(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if (!Character.isWhitespace(text.charAt(i)))
            {
                return true;
            }
        }
        return false;
    }

    private static int getWordStart(String text, int cursor)
    {
        int i = cursor;
        while (i > 0 && !Character.isWhitespace(text.charAt(i - 1)))
        {
            i--;
        }
        return i;
    }
}
