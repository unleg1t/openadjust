package net.minecraft.client.gui;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.ChatAutocompleteEvent;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.modules.impl.render.hud.GlobalChatMode;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.modules.impl.render.tweaks.TweaksModule;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.chat.ChatChannel;
import com.adjustclient.core.gui.chat.ChatChannelTabs;
import com.adjustclient.core.gui.chat.ChatCommandSuggestions;
import com.adjustclient.core.gui.chat.ChatLineActions;
import com.adjustclient.utilities.KeyboardUtil;
import com.adjustclient.utilities.PlayerNameCompleter;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiChat extends GuiScreen
{
    private static final Logger logger = LogManager.getLogger();
    private String historyBuffer = "";
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    private List<String> foundPlayerNames = Lists.<String>newArrayList();
    public CommandSuggestions commandSuggestions = new CommandSuggestions(this);
    public GuiTextField inputField;
    private String defaultInputFieldText = "";
    public ChatCommandSuggestions chatCommandSuggestions;
    public ChatChannelTabs chatChannelTabs = new ChatChannelTabs();
    private char openingKeyChar;
    private int openingKeyCode = -1;
    private long openingTimeMillis;

    public GuiChat()
    {
    }

    public GuiChat(String defaultText)
    {
        this.defaultInputFieldText = defaultText;
    }

    public GuiChat(String defaultText, int keyCode, char keyChar)
    {
        this.defaultInputFieldText = defaultText;
        this.openingKeyCode = keyCode;
        this.openingKeyChar = keyChar;
        this.openingTimeMillis = System.currentTimeMillis();
    }

    public ChatCommandSuggestions getChatCommandSuggestions()
    {
        if (this.chatCommandSuggestions == null)
        {
            this.chatCommandSuggestions = new ChatCommandSuggestions();
            if (Adjust.INSTANCE != null)
            {
                this.chatCommandSuggestions.commands = Adjust.INSTANCE.commandManager;
            }
        }
        return this.chatCommandSuggestions;
    }

    private GuiPersistentChat getActiveChat()
    {
        if (Adjust.chatManager != null && Adjust.chatManager.selected != null && Adjust.chatManager.selected.history != null)
        {
            return Adjust.chatManager.selected.history;
        }
        return this.mc.ingameGUI.getChatGUI();
    }

    private static boolean isMinecraftChannelSelected()
    {
        return Adjust.chatManager == null || Adjust.chatManager.selected == Adjust.chatManager.minecraftChannel;
    }

    private static boolean isOnHypixel()
    {
        return ServerTracker.INSTANCE != null
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }

    private static boolean isHypixelPlayerAutocompleteCommand(String text)
    {
        return text.startsWith("/shout ")
                || text.startsWith("/pc ")
                || text.startsWith("/ac ")
                || text.startsWith("/gc ")
                || text.startsWith("/wdr ")
                || text.startsWith("/f ")
                || text.startsWith("/p ")
                || text.startsWith("/friend ")
                || text.startsWith("/party ")
                || text.startsWith("/msg ")
                || text.startsWith("/r ");
    }

    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.getActiveChat().getSentMessages().size();
        this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(100);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
        if (this.chatChannelTabs == null)
        {
            this.chatChannelTabs = new ChatChannelTabs();
        }
        this.chatChannelTabs.x = 5.0F;
        this.chatChannelTabs.y = (float) this.height - 22.5F;
        this.chatChannelTabs.rebuild();
        if (this.commandSuggestions == null)
        {
            this.commandSuggestions = new CommandSuggestions(this);
        }
        else
        {
            this.commandSuggestions.reset();
        }
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        this.getActiveChat().resetScroll();
    }

    public void updateScreen()
    {
        ChatCommandSuggestions suggestions = this.getChatCommandSuggestions();
        if (suggestions != null && suggestions.commands != null
                && ChatCommandSuggestions.slot819b != null
                && ChatCommandSuggestions.slot819b.field12 != null
                && ChatCommandSuggestions.slot819b.field12.field4 != null)
        {
            suggestions.updateInput(this);
        }
        this.inputField.updateCursorCounter();
        if (this.commandSuggestions != null)
        {
            this.commandSuggestions.tick();
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.openingKeyChar != 0)
        {
            if (this.openingTimeMillis + 10L <= System.currentTimeMillis() && keyCode == this.openingKeyCode)
            {
                this.openingKeyChar = 0;
            }
            else if (Character.toLowerCase(typedChar) == Character.toLowerCase(this.openingKeyChar)
                    && this.openingTimeMillis + 500L > System.currentTimeMillis())
            {
                return;
            }
            else
            {
                this.openingKeyChar = 0;
            }
        }

        if (keyCode == 15 && KeyboardUtil.isKeyDown()
                && Adjust.chatManager != null
                && Adjust.chatManager.channels != null
                && !Adjust.chatManager.channels.isEmpty())
        {
            int nextIndex = Adjust.chatManager.channels.indexOf(Adjust.chatManager.selected) + 1;
            if (nextIndex < 0 || nextIndex >= Adjust.chatManager.channels.size())
            {
                nextIndex = 0;
            }
            Adjust.chatManager.selectChannel(Adjust.chatManager.channels.get(nextIndex));
            return;
        }

        this.waitingOnAutocomplete = false;

        if (keyCode == 15)
        {
            if (this.commandSuggestions != null && this.commandSuggestions.handleKey(keyCode))
            {
                return;
            }
            this.autocompletePlayerNames();
        }
        else
        {
            this.playerNamesFound = false;
        }

        if (this.commandSuggestions != null && this.commandSuggestions.handleKey(keyCode))
        {
            return;
        }

        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 200)
            {
                this.getSentHistory(-1);
            }
            else if (keyCode == 208)
            {
                this.getSentHistory(1);
            }
            else if (keyCode == 201)
            {
                this.getActiveChat().scroll(this.getActiveChat().getLineCount() - 1);
            }
            else if (keyCode == 209)
            {
                this.getActiveChat().scroll(-this.getActiveChat().getLineCount() + 1);
            }
            else
            {
                String previousText = this.inputField.getText();
                this.inputField.textboxKeyTyped(typedChar, keyCode);
                if (this.commandSuggestions != null && !this.inputField.getText().equals(previousText))
                {
                    this.commandSuggestions.onInputChanged();
                }
            }
        }
        else
        {
            String s = this.inputField.getText().trim();
            this.mc.displayGuiScreen((GuiScreen)null);

            if (s.length() > 0)
            {
                String commandPrefix = UserConfiguration.COMMAND_PREFIX != null
                        ? UserConfiguration.COMMAND_PREFIX.serialize()
                        : ".";
                ChatChannel channel = null;
                if (Adjust.chatManager != null)
                {
                    channel = (s.startsWith("/") || s.startsWith(commandPrefix))
                            ? Adjust.chatManager.minecraftChannel
                            : Adjust.chatManager.selected;
                }

                GuiPersistentChat targetChat = (channel != null && channel.history != null)
                        ? channel.history
                        : this.mc.ingameGUI.getChatGUI();
                targetChat.addToSentMessages(s);

                if (UserConfiguration.chatMode != null
                        && UserConfiguration.chatMode.getValue() == GlobalChatMode.ALWAYS
                        && this.isIrcInput())
                {
                    if (s.length() > 1 && Adjust.chatManager != null && Adjust.chatManager.globalChannel != null)
                    {
                        Adjust.chatManager.globalChannel.sendMessage(s.substring(1));
                    }
                    return;
                }

                if (channel != null && channel.sender != null)
                {
                    channel.sender.accept(s);
                }
                else
                {
                    if (StreamerModule.nameMap != null)
                    {
                        for (String name : PlayerNameCompleter.complete(null, true))
                        {
                            s = StreamerModule.restoreNameInText(s, name);
                        }
                    }
                    this.sendChatMessage(s, true);
                }
            }
        }
    }

    public void handleMouseInput() throws IOException
    {
        int i = Mouse.getEventDWheel();

        if (i != 0)
        {
            if (i > 1)
            {
                i = 1;
            }

            if (i < -1)
            {
                i = -1;
            }

            if (this.commandSuggestions != null && this.commandSuggestions.handleMouseWheel(i))
            {
                super.handleMouseInput();
                return;
            }

            if (!isShiftKeyDown())
            {
                i *= 7;
            }

            this.getActiveChat().scroll(i);
        }

        super.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0
                && TweaksModule.INSTANCE != null
                && TweaksModule.INSTANCE.chat != null
                && TweaksModule.INSTANCE.chat.isChatEnabled()
                && this.commandSuggestions != null
                && this.commandSuggestions.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return;
        }

        if (this.chatChannelTabs != null && this.chatChannelTabs.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return;
        }

        GuiPersistentChat activeChat = this.getActiveChat();
        if (ChatLineActions.mouseClicked(activeChat, mouseX, mouseY, this.height, mouseButton))
        {
            return;
        }

        if (mouseButton == 0)
        {
            IChatComponent ichatcomponent = activeChat.getChatComponent(Mouse.getX(), Mouse.getY());

            if (this.handleComponentClick(ichatcomponent))
            {
                return;
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void setText(String newChatText, boolean shouldOverwrite)
    {
        if (shouldOverwrite)
        {
            this.inputField.setText(newChatText);
        }
        else
        {
            this.inputField.writeText(newChatText);
        }
    }

    public void autocompletePlayerNames()
    {
        if (this.playerNamesFound)
        {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

            if (this.autocompleteIndex >= this.foundPlayerNames.size())
            {
                this.autocompleteIndex = 0;
            }
        }
        else
        {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.foundPlayerNames.clear();
            this.autocompleteIndex = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.sendAutocompleteRequest(s1, s);

            if (this.foundPlayerNames.isEmpty())
            {
                return;
            }

            this.playerNamesFound = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.foundPlayerNames.size() > 1)
        {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : this.foundPlayerNames)
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(s2);
            }

            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        this.inputField.writeText((String)this.foundPlayerNames.get(this.autocompleteIndex++));
    }

    private void sendAutocompleteRequest(String p_146405_1_, String p_146405_2_)
    {
        if (p_146405_1_.length() >= 1)
        {
            if (UserConfiguration.COMMAND_PREFIX != null
                    && p_146405_1_.charAt(0) == UserConfiguration.COMMAND_PREFIX.getChar())
            {
                String[] args = p_146405_1_.contains(" ")
                        ? p_146405_1_.substring(p_146405_1_.indexOf(' ') + 1).split(" ", -1)
                        : new String[0];
                String[] completions = this.getChatCommandSuggestions().complete(p_146405_1_, args);
                if (this.commandSuggestions != null)
                {
                    this.commandSuggestions.onCompletionResponse(completions);
                }
                return;
            }

            ChatAutocompleteEvent event = new ChatAutocompleteEvent(p_146405_1_, p_146405_2_);
            EventBus.post(event);
            if (event.completions != null)
            {
                if (this.commandSuggestions != null)
                {
                    this.commandSuggestions.onCompletionResponse(event.completions);
                }
                return;
            }

            if (isOnHypixel() && this.inputField != null)
            {
                String fullText = this.inputField.getText();
                if (!fullText.startsWith("/") || isHypixelPlayerAutocompleteCommand(fullText))
                {
                    int space = fullText.lastIndexOf(' ');
                    String token = (space == -1 ? fullText : fullText.substring(space + 1)).toLowerCase(Locale.ROOT);
                    if (this.commandSuggestions != null)
                    {
                        this.commandSuggestions.onCompletionResponse(PlayerNameCompleter.complete(token, true));
                    }
                    return;
                }
            }

            BlockPos blockpos = null;

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                blockpos = this.mc.objectMouseOver.getBlockPos();
            }

            if (this.mc.thePlayer != null && this.mc.thePlayer.sendQueue != null)
            {
                String requestText = Adjust.chatTextReplacer != null
                        ? Adjust.chatTextReplacer.replace(p_146405_1_)
                        : p_146405_1_;
                this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(requestText, blockpos));
                this.waitingOnAutocomplete = true;
            }
        }
    }

    boolean isCommandInput()
    {
        if (this.inputField == null)
        {
            return false;
        }

        String text = this.inputField.getText();
        if (text.isEmpty())
        {
            return false;
        }

        if (text.charAt(0) == '/')
        {
            return true;
        }

        return Adjust.INSTANCE != null
                && Adjust.INSTANCE.commandManager != null
                && UserConfiguration.COMMAND_PREFIX != null
                && text.charAt(0) == UserConfiguration.COMMAND_PREFIX.getChar();
    }

    boolean shouldAutoRequestCompletions()
    {
        if (this.inputField == null)
        {
            return false;
        }

        String text = this.inputField.getText();
        if (text.isEmpty())
        {
            return false;
        }

        if (UserConfiguration.COMMAND_PREFIX != null
                && text.charAt(0) == UserConfiguration.COMMAND_PREFIX.getChar())
        {
            return true;
        }

        if (!isMinecraftChannelSelected() || !isOnHypixel())
        {
            return false;
        }

        return !text.startsWith("/") || isHypixelPlayerAutocompleteCommand(text);
    }

    boolean isIrcInput()
    {
        if (this.inputField == null || !isMinecraftChannelSelected() || UserConfiguration.IRC_PREFIX == null)
        {
            return false;
        }

        String text = this.inputField.getText();
        return text.startsWith(UserConfiguration.IRC_PREFIX.serialize());
    }

    boolean tryLocalAutocomplete(String prefix, String wordPrefix)
    {
        if (prefix.length() < 1)
        {
            return false;
        }

        this.sendAutocompleteRequest(prefix, wordPrefix);
        return true;
    }

    public void getSentHistory(int msgPos)
    {
        GuiPersistentChat activeChat = this.getActiveChat();
        int i = this.sentHistoryCursor + msgPos;
        int j = activeChat.getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != this.sentHistoryCursor)
        {
            if (i == j)
            {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            }
            else
            {
                if (this.sentHistoryCursor == j)
                {
                    this.historyBuffer = this.inputField.getText();
                }

                this.inputField.setText((String)activeChat.getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.chatChannelTabs != null)
        {
            this.chatChannelTabs.draw();
        }

        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

        if (this.inputField != null)
        {
            String commandPrefix = UserConfiguration.COMMAND_PREFIX != null
                    ? UserConfiguration.COMMAND_PREFIX.serialize()
                    : ".";
            if (this.inputField.getText().startsWith(commandPrefix))
            {
                this.getChatCommandSuggestions().updateInputColor(this);
            }
            else if (this.isIrcInput())
            {
                this.inputField.setTextColor(this.fontRendererObj.getColorCode('6'));
            }
            else
            {
                this.inputField.setTextColor(-1);
            }
        }

        this.inputField.drawTextBox();

        GuiPersistentChat activeChat = this.getActiveChat();
        IChatComponent ichatcomponent = activeChat.getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null)
        {
            this.handleComponentHover(ichatcomponent, mouseX, mouseY);
        }

        List<String> actions = ChatLineActions.drawActions(activeChat, mouseX, mouseY, this.height);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.commandSuggestions != null)
        {
            this.commandSuggestions.drawScreen(mouseX, mouseY);
        }

        if (actions != null && !actions.isEmpty())
        {
            this.drawHoveringText(actions, mouseX, mouseY);
        }
    }

    @Override
    protected void drawHoveringText(List<String> textLines, int x, int y)
    {
        NvgContext.flushFrame();
        super.drawHoveringText(textLines, x, y);
    }

    public void onAutocompleteResponse(String[] p_146406_1_)
    {
        if (this.commandSuggestions != null && this.commandSuggestions.awaitingCompletionResponse)
        {
            this.commandSuggestions.onCompletionResponse(p_146406_1_);
        }

        if (this.waitingOnAutocomplete)
        {
            this.playerNamesFound = false;
            this.foundPlayerNames.clear();

            for (String s : p_146406_1_)
            {
                if (s.length() > 0)
                {
                    this.foundPlayerNames.add(s);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(p_146406_1_);

            if (s2.length() > 0 && !s1.equalsIgnoreCase(s2))
            {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            }
            else if (this.foundPlayerNames.size() > 0)
            {
                this.playerNamesFound = true;
                this.autocompletePlayerNames();
            }
        }
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
