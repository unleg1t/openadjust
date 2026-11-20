package com.adjustclient.modules;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.BindMode;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.settings.property.KeybindManager;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.SettingContainer;
import com.adjustclient.core.gui.clickgui.ClickGuiItem;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ErrorReport;
import com.adjustclient.utilities.MinecraftInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChatColor;

public abstract class Module implements Listener, MinecraftInstance, SettingContainer, ClickGuiItem {
    public final String name;
    public final Category category;
    public final Bind bind;
    protected final ArrayList<Setting<?>> settings;
    public Object field20;
    public Object field24;
    public byte flags;
    public byte savedFlags;
    protected boolean notifyToggle;
    public boolean field31;

    protected Module(String name, Category category) {
        settings = new ArrayList<>();
        notifyToggle = true;
        field31 = false;
        this.name = name;
        this.category = category;
        bind = Adjust.INSTANCE.bindManager.register("module_toggle." + name,
                mode -> {
                    if (mode == BindMode.PRESSED) toggle(!isEnabled());
                }, name);
        setHidden(true);
    }

    public boolean canToggle(boolean enabling) { return true; }

    public void syncHidden() {
        setHidden((savedFlags & 4) != 0);
        flags = (byte) ((flags & ~8) | (savedFlags & 8));
        toggle((savedFlags & 2) != 0);
    }

    public void onEnable() { }

    public void onDisable() { }

    public String getSuffix() { return null; }

    public String getName() { return name; }

    public String getDescription() { return null; }

    public String getAdditionalDescription() { return null; }

    public List<Setting<?>> getSettings() { return settings; }

    @Override public String toString() { return name; }

    public void disableConflictingModules(Class<? extends Module>... types) {
        toggle(false);
        for (Class<? extends Module> type : types) {
            Module other = Adjust.INSTANCE.moduleManager.getModule(type);
            if (other != this && other.bind.intersects(bind)) {
                other.toggle(false);
            }
        }
    }

    public void reportToggleError(Throwable error, boolean enabling) {
        String message = "{} module (" + error + ") {} caused an exception. Please report this!";
        Object[] arguments = {enabling ? "Enabling" : "Disabling", name};
        Logger.log(LogLevel.ERROR, true, message, arguments);
        NotificationBuilder notification = new ErrorReport(message, arguments).notification("Error");
        notification.lifetimeSteps = 200;
        notification.show();
    }

    public Setting<?> findSetting(List<Setting<?>> settings, String path) {
        if (path == null) return null;
        if (settings == null) settings = this.settings;
        int separator = path.indexOf("->");
        if (separator != -1) {
            ExpandSetting group = (ExpandSetting) findSetting(settings, path.substring(0, separator));
            if (group == null) return null;
            return findSetting(group.getSettings(), path.substring(separator + 2));
        }
        boolean useDisplayName = path.startsWith("^");
        String target = useDisplayName ? path.substring(1) : path;
        return settings.stream().filter(setting ->
                (useDisplayName ? setting.getDisplayName() : setting.getName()).equalsIgnoreCase(target))
                .findFirst().orElse(null);
    }

    public void toggle(boolean enabled) {
        setEnabled(enabled, true);
    }

    public void toggle() { toggle(!isEnabled()); }

    public ChatMessage getFormattedName() {
        ChatMessage message = new ChatMessage();
        message.color = ChatColor.GRAY;
        message.component = new ChatComponentText("");
        message.component.appendSibling(new ChatComponentText(name + " ")
                .setChatStyle(new ChatStyle().setBold(true)));
        return message;
    }

    public void setEnabled(boolean enabled, boolean notify) {
        if (isEnabled() == enabled || !canToggle(enabled)) return;
        flags = (byte) (enabled ? flags | 2 : flags & ~2);
        if (enabled) onEnable();
        else onDisable();
        if (notify && notifyToggle) Adjust.INSTANCE.fileSystem.markDirty();
    }

    public void setHidden(boolean hidden) {
        if (((flags & 4) != 0) == hidden) return;
        flags = (byte) (hidden ? flags | 4 : flags & ~4);
        Adjust.INSTANCE.fileSystem.markDirty();
    }

    public boolean isEnabled() { return (flags & 2) != 0; }
    public Category getCategory() { return category; }
    public Bind getBind() { return bind; }
}
