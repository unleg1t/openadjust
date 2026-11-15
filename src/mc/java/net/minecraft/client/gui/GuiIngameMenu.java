package net.minecraft.client.gui;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.render.tweaks.TweaksModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;
import com.adjustclient.ui.stats.HypixelStatsScreen;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import java.io.IOException;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.ChatColor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIngameMenu extends GuiScreen
{
    private int field_146445_a;
    private int field_146444_f;
    private final Timer disconnectConfirmTimer = new Timer(0L);
    private String disconnectButtonText = "";

    public void initGui()
    {
        this.field_146445_a = 0;
        this.buttonList.clear();
        int i = -16;
        int j = 98;
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + i, I18n.format("menu.returnToMenu", new Object[0])));

        if (!this.mc.isIntegratedServerRunning())
        {
            ((GuiButton)this.buttonList.get(0)).displayString = I18n.format("menu.disconnect", new Object[0]);
        }

        this.disconnectButtonText = ((GuiButton)this.buttonList.get(0)).displayString;
        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + i, I18n.format("menu.returnToGame", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.options", new Object[0])));
        GuiButton guibutton;
        this.buttonList.add(guibutton = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.shareToLan", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.achievements", new Object[0])));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.stats", new Object[0])));
        this.buttonList.add(new GuiButton(8, this.width / 2 - 100, this.height / 4 + 56, "Account Manager"));
        guibutton.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 1:
                if (TweaksModule.INSTANCE != null
                        && TweaksModule.INSTANCE.disconnectConfirm != null
                        && TweaksModule.INSTANCE.disconnectConfirm.isEnabled()
                        && this.disconnectConfirmTimer.hasElapsed(1500L))
                {
                    this.disconnectConfirmTimer.reset();
                    return;
                }

                boolean flag = this.mc.isIntegratedServerRunning();
                boolean flag1 = this.mc.isConnectedToRealms();
                button.enabled = false;
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient)null);

                if (flag)
                {
                    this.mc.displayGuiScreen(new AdjustMainMenu());
                }
                else if (flag1)
                {
                    RealmsBridge realmsbridge = new RealmsBridge();
                    realmsbridge.switchToRealms(new AdjustMainMenu());
                }
                else
                {
                    this.mc.displayGuiScreen(new GuiMultiplayer(new AdjustMainMenu()));
                }

            case 2:
            case 3:
            default:
                break;
            case 4:
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;
            case 5:
                this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                break;
            case 6:
                if (ServerTracker.INSTANCE.serverState != null
                        && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                        && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL)
                {
                    this.mc.displayGuiScreen(new HypixelStatsScreen((GuiScreen)null, this.mc.thePlayer.getUniqueID().toString()));
                }
                else
                {
                    this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
                }
                break;
            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case 8:
                this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
                break;
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.field_146444_f;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        if (!this.buttonList.isEmpty())
        {
            GuiButton disconnectButton = (GuiButton)this.buttonList.get(0);

            if (!this.disconnectConfirmTimer.hasElapsed(1500L))
            {
                long remaining = 1500L - (System.currentTimeMillis() - this.disconnectConfirmTimer.lastMs);
                disconnectButton.displayString = this.disconnectButtonText + ChatColor.GRAY + " (" + TimeUtil.formatDuration(remaining, true) + ")";
            }
            else
            {
                disconnectButton.displayString = this.disconnectButtonText;
            }
        }

        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
