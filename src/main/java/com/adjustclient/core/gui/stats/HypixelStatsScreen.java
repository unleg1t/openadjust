package com.adjustclient.core.gui.stats;

import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayerStats;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelSocialLinks;
import com.mojang.util.UUIDTypeAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.util.ChatColor;
import com.adjustclient.Adjust;

public final class HypixelStatsScreen extends GuiScreen {
    private boolean noPlayer;
    public final List<GuiButton> buttons = buttonList;
    public final List<Pair<String, String>> rows = new ArrayList<>();
    private final GuiScreen parent;
    final String uuid;
    public HypixelPlayer player;
    private StatsList general;
    private SkyWarsStatsList skyWars;
    private BedwarsStatsList bedwars;
    private DuelsStatsList duels;
    private BlitzStatsList blitz;
    private UhcStatsList uhc;
    private SocialMediaList social;
    StatsList selected;
    public GuiButton generalButton;
    public GuiButton skyWarsButton;
    public GuiButton bedwarsButton;
    public GuiButton duelsButton;
    public GuiButton blitzButton;
    public GuiButton uhcButton;
    public GuiButton socialButton;

    public HypixelStatsScreen(GuiScreen parent, String uuid) {
        this.mc = Minecraft.getMinecraft();
        java.util.UUID targetUuid;
        if (uuid == null || uuid.isEmpty()) {
            targetUuid = net.minecraft.entity.player.EntityPlayer.getOfflineUUID("");
        } else {
            try {
                targetUuid = UUIDTypeAdapter.fromString(uuid);
            } catch (IllegalArgumentException ignored) {
                targetUuid = net.minecraft.entity.player.EntityPlayer.getOfflineUUID(uuid);
            }
        }
        Adjust.hypixelApiManager.getPlayer(targetUuid)
                .thenAccept(this::setPlayer);
        this.parent = parent;
        this.uuid = uuid;
    }

    public void selectList(StatsList list) {
        if (selected == list) return;
        rows.clear();
        list.populate();
        selected = list;
    }

    public void setPlayer(HypixelPlayer player) {
        this.player = player;
        if (player != null) {
            createLists();
        } else {
            buttonList.clear();
            buttonList.add(new GuiButton(0, width / 2 - 75, height / 2 + 20, 150, 20, I18n.format("gui.done")));
            noPlayer = true;
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        switch (button.id) {
            case 0: mc.displayGuiScreen(parent); break;
            case 1: selectList(general); break;
            case 2: selectList(skyWars); break;
            case 3: selectList(bedwars); break;
            case 4: selectList(duels); break;
            case 5: selectList(blitz); break;
            case 6: selectList(uhc); break;
            case 7: selectList(social); break;
            default: selected.actionPerformed(button); break;
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc == null) return;
        if (mc.theWorld != null) drawRect(0, 0, width, height, (int) 3222278160L);
        else drawDefaultBackground();
        if (fontRendererObj == null) fontRendererObj = mc.fontRendererObj;
        if (selected != null) {
            selected.drawScreen(mouseX, mouseY, partialTicks);
            drawCenteredString(fontRendererObj, "Hypixel Statistics", width / 2, 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, ChatColor.GRAY + selected.getName(), width / 2, 20, 0xFFFFFF);
        } else if (noPlayer) {
            drawCenteredString(fontRendererObj, "This player has never joined Hypixel.", width / 2, height / 2, 0xFFFFFF);
        } else {
            drawCenteredString(fontRendererObj, "Fetching...", width / 2, height / 2, 0xFFFFFF);
            String[] animation = {"oooooo", "Oooooo", "oOoooo", "ooOooo", "oooOoo", "ooooOo", "oooooO"};
            drawCenteredString(fontRendererObj, animation[(int) (Minecraft.getSystemTime() / 150L % 7L)],
                    width / 2, height / 2 + 2 * fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void initGui() {
        if (player != null) createLists();
        else if (noPlayer) mc.displayGuiScreen(parent);
    }

    public boolean isUnavailable(GuiButton button) {
        HypixelPlayerStats stats = player.stats;
        if (button == skyWarsButton) return stats == null || stats.skyWars == null;
        if (button == bedwarsButton) return stats == null || stats.bedwars == null;
        if (button == duelsButton) return stats == null || stats.duels == null;
        if (button == blitzButton) return stats == null || stats.hungerGames == null;
        if (button == uhcButton) return stats == null || stats.uhc == null;
        if (button == socialButton) {
            if (player.socialMedia == null || player.socialMedia.links == null) return true;
            HypixelSocialLinks links = player.socialMedia.links;
            return links.youtube == null && links.twitch == null && links.twitter == null
                    && links.discord == null && links.hypixel == null;
        }
        return false;
    }

    private void createLists() {
        resizeLists();
        selectList(general);
        createButtons();
    }

    private void resizeLists() {
        general = new StatsList(this);
        skyWars = new SkyWarsStatsList(this);
        bedwars = new BedwarsStatsList(this);
        duels = new DuelsStatsList(this);
        blitz = new BlitzStatsList(this);
        uhc = new UhcStatsList(this);
        social = new SocialMediaList(this);
    }

    private void createButtons() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 75, height - 28, 150, 20, I18n.format("gui.done")));
        int x = (width - 584) / 2;
        generalButton = new GuiButton(1, x, height - 52, 80, 20, "General");
        buttonList.add(generalButton);
        generalButton.enabled = false;
        skyWarsButton = new GuiButton(2, x + 84, height - 52, 80, 20, "Sky Wars");
        buttonList.add(skyWarsButton);
        bedwarsButton = new GuiButton(3, x + 168, height - 52, 80, 20, "Bedwars");
        buttonList.add(bedwarsButton);
        duelsButton = new GuiButton(4, x + 252, height - 52, 80, 20, "Duels");
        buttonList.add(duelsButton);
        blitzButton = new GuiButton(5, x + 336, height - 52, 80, 20, "Blitz SG");
        buttonList.add(blitzButton);
        uhcButton = new GuiButton(6, x + 420, height - 52, 80, 20, "UHC");
        buttonList.add(uhcButton);
        socialButton = new GuiButton(7, x + 504, height - 52, 80, 20, "Social Media");
        buttonList.add(socialButton);
        for (GuiButton button : buttonList) if (isUnavailable(button)) button.enabled = false;
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (selected != null) selected.handleMouseInput();
    }
}
