package net.minecraft.client.gui;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.AccountManagerScreen;
import com.adjustclient.utilities.TimeUtil;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiDisconnected extends GuiScreen
{
    private static final Pattern BLOCKED_USERNAME_PATTERN = Pattern.compile("Your username, (.*?), is not allowed on the server and is breaking our rules.\n");
    private String reason;
    private IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;
    public boolean forceReconnect = false;
    private GuiButton refreshSessionButton;
    private final Timer reconnectTimer = new Timer();
    private GuiButton reconnectButton;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp)
    {
        this.parentScreen = screen;
        this.reason = I18n.format(reasonLocalizationKey, new Object[0]);
        this.message = chatComp;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE && this.reconnectTimer.hasElapsed(1600L))
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    public void updateScreen()
    {
        this.updateReconnectButton();
    }

    private void updateReconnectButton()
    {
        if (this.reconnectButton == null)
        {
            return;
        }

        if (!this.forceReconnect)
        {
            this.reconnectButton.displayString = "Reconnect";
            this.reconnectButton.enabled = true;
            return;
        }

        this.reconnectButton.displayString = "Ignore & Reconnect";

        if (!this.reconnectTimer.hasElapsed(2000L))
        {
            this.reconnectButton.enabled = false;
            long remaining = 2000L - (System.currentTimeMillis() - this.reconnectTimer.lastMs);
            this.reconnectButton.displayString = this.reconnectButton.displayString + " " + ChatColor.GRAY + "(" + TimeUtil.formatDuration(remaining, true) + ")";
        }
        else
        {
            this.reconnectButton.enabled = true;
        }
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.field_175353_i = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu", new Object[0])));

        if (ServerTracker.INSTANCE.field24 != null && ServerTracker.INSTANCE.field28 instanceof ServerData)
        {
            this.reconnectButton = new GuiButton(1, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 24, this.forceReconnect ? "Ignore & Reconnect" : "Reconnect");
            this.buttonList.add(this.reconnectButton);
            this.updateReconnectButton();
        }
        else
        {
            this.reconnectButton = null;
        }

        if (this.canRefreshSession())
        {
            this.refreshSessionButton = new GuiButton(2, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 24 * this.buttonList.size(), "Refresh session");
            this.buttonList.add(this.refreshSessionButton);
            return;
        }

        this.refreshSessionButton = null;

        if (ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && this.matchesSelectedAccount()
                && this.message.getSiblings().size() >= 3)
        {
            IChatComponent firstSibling = (IChatComponent)this.message.getSiblings().get(0);
            IChatComponent thirdSibling = (IChatComponent)this.message.getSiblings().get(2);

            if ("You are currently blocked from joining this server!\n\n".equals(firstSibling.getUnformattedTextForChat())
                    && BLOCKED_USERNAME_PATTERN.matcher(thirdSibling.getUnformattedTextForChat()).matches())
            {
                this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 24 * this.buttonList.size(), "Change username"));
            }
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 1)
        {
            if (ServerTracker.INSTANCE.field28 instanceof ServerData)
            {
                GuiConnecting connecting = new GuiConnecting(this.parentScreen, this.mc, (ServerData)ServerTracker.INSTANCE.field28);

                if (this.forceReconnect)
                {
                    connecting.forceConnect = true;
                }

                this.mc.displayGuiScreen(connecting);
            }
        }
        else if (button.id == 2)
        {
            if (this.canRefreshSession())
            {
                AccountEntry selectedAccount = Adjust.INSTANCE.accountManager.selectedAccount;

                if (selectedAccount != null)
                {
                    selectedAccount.requestAccountData((success) -> {
                        if (this.refreshSessionButton != null)
                        {
                            this.refreshSessionButton.enabled = true;
                        }

                        if (Boolean.TRUE.equals(success) && this.refreshSessionButton != null)
                        {
                            this.buttonList.remove(this.refreshSessionButton);
                        }
                    });

                    if (this.refreshSessionButton != null)
                    {
                        this.refreshSessionButton.enabled = false;
                    }
                }
            }
        }
        else if (button.id == 3)
        {
            AccountManagerScreen accountManager = Adjust.INSTANCE != null ? Adjust.INSTANCE.accountManager : null;

            if (accountManager != null && accountManager.isSessionAccountSelected())
            {
                this.mc.displayGuiScreen(accountManager.viewAccountScreen);
                accountManager.viewAccountScreen.setEditing(true);
            }
        }
    }

    private boolean canRefreshSession()
    {
        if (!(this.message instanceof ChatComponentTranslation))
        {
            return false;
        }

        if (!"disconnect.loginFailedInfo".equals(((ChatComponentTranslation)this.message).getKey()))
        {
            return false;
        }

        return this.matchesSelectedAccount();
    }

    private boolean matchesSelectedAccount()
    {
        AccountManagerScreen accountManager = Adjust.INSTANCE != null ? Adjust.INSTANCE.accountManager : null;

        if (accountManager == null || accountManager.selectedAccount == null || accountManager.currentRepository == null || this.mc == null)
        {
            return false;
        }

        Session session = this.mc.getSession();
        return session != null
                && accountManager.selectedAccount.username.equals(session.getUsername())
                && accountManager.selectedAccount.uuid.equals(session.getPlayerID());
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.reason, this.width / 2, this.height / 2 - this.field_175353_i / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int i = this.height / 2 - this.field_175353_i / 2;

        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
                i += this.fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
