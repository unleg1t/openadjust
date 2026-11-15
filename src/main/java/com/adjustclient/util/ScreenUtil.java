package com.adjustclient.util;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import com.adjustclient.Adjust;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.module.impl.render.ClickGui;
import com.adjustclient.setting.Setting;
import com.adjustclient.ui.screen.AdjustScreen;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.overlay.DimOverlay;
import com.adjustclient.ui.clickgui.overlay.ScrollToComponentOverlay;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.ui.friends.FriendsScreen;
import com.adjustclient.ui.config.GlobalConfigsScreen;

public final class ScreenUtil implements MinecraftInstance {
    public static boolean isScreenOpen() {
        return mc.currentScreen != null || Adjust.INSTANCE.screenStack.getTopOverlay() != null
                || Adjust.inventoryStateTracker.open;
    }
    public static boolean isContainerOpen(Class<?>... types) {
        if (mc.currentScreen == null) return false;
        for (Class<?> type : types) if (type.isInstance(mc.currentScreen)) return true;
        return false;
    }
    public static void openClickGuiAt(com.adjustclient.ui.clickgui.ClickGuiItem setting) {
        boolean alreadyOpen = ClickGui.isOpen();
        ClickGuiScreen screen = ClickGui.openClickGui();
        ClickGuiComponent component = screen.componentMap.get(setting);
        if (component == null) {
            Logger.log(LogLevel.WARN, true, "Unable to associate property {} with a component",
                    setting instanceof com.adjustclient.module.Module module ? module.getName() : ((Setting<?>) setting).getName());
            return;
        }
        screen.addOverlay(new ScrollToComponentOverlay(component));
        component.expandAncestors();
        if (!alreadyOpen) screen.addOverlay(new DimOverlay(component.bounds));
    }
    public static boolean isAnyScreenOpen() {
        return (mc != null && mc.currentScreen != null) || isAdjustScreenOpen();
    }
    public static boolean isAccountScreenOpen() {
        return isContainerOpen(com.adjustclient.ui.account.importer.BulkImportScreen.class,
                com.adjustclient.ui.account.view.CreateProfileScreen.class,
                com.adjustclient.ui.account.repository.CreateRepositoryScreen.class,
                com.adjustclient.ui.account.view.ViewAccountScreen.class,
                com.adjustclient.ui.account.repository.RepositoryMembersScreen.class,
                com.adjustclient.ui.account.login.AddAccountScreen.class,
                com.adjustclient.ui.account.AccountManagerScreen.class,
                com.adjustclient.ui.account.repository.RepositoryListScreen.class,
                com.adjustclient.ui.account.repository.SelectRepositoryScreen.class);
    }
    public static boolean isAdjustScreenOpen() {
        return Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null
                && Adjust.INSTANCE.screenStack.getTopOverlay() != null;
    }
    public static boolean isSettingsScreenOpen() {
        return isContainerOpen(GuiSnooper.class, net.minecraft.client.gui.achievement.GuiAchievements.class,
                GuiScreenResourcePacks.class, GuiControls.class, optifine.GuiQualitySettingsOF.class,
                shadersmod.client.GuiShaders.class, GuiIngameMenu.class, ScreenChatOptions.class,
                GuiScreenServerList.class, optifine.GuiPerformanceSettingsOF.class,
                optifine.GuiOtherSettingsOF.class, optifine.GuiAnimationSettingsOF.class,
                optifine.GuiDetailSettingsOF.class, GuiVideoSettings.class, GuiLanguage.class,
                GuiScreenOptionsSounds.class, GuiCustomizeSkin.class, GuiOptions.class);
    }
    public static boolean canMoveInScreen() {
        if (mc != null && mc.currentScreen != null && mc.currentScreen.getClass() == GuiContainerCreative.class
                && GuiContainerCreative.selectedTabIndex == 5) return true;
        if (Adjust.INSTANCE == null || Adjust.INSTANCE.screenStack == null || Adjust.INSTANCE.screenStack.partyService == null) return false;
        var party = Adjust.INSTANCE.screenStack.partyService.party;
        if (party != null && party.chat.input.focused) return true;
        if (mc.currentScreen instanceof GuiChat) return true;
        AdjustScreen top = Adjust.INSTANCE.screenStack.getTopOverlay();
        if (top instanceof DialogScreen && ((DialogScreen) top).hasFocusedInput()) return true;
        if (top instanceof ClickGuiScreen && ((ClickGuiScreen) top).searchBar.active) return true;
        if (top instanceof GlobalConfigsScreen && ((GlobalConfigsScreen) top).hasFocusedSearch()) return true;
        if (top instanceof FriendsScreen && ((FriendsScreen) top).detailsPane.input.focused) return true;
        return Adjust.INSTANCE.screenStack.overlays.contains(com.adjustclient.service.party.PartyHudScreen.INSTANCE);
    }
}
