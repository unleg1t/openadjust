package com.adjustclient.changelog;

import com.adjustclient.Adjust;
import com.adjustclient.module.Module;
import com.adjustclient.command.CommandBase;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.module.impl.render.ClickGui;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.util.BrowserUtil;
import com.adjustclient.util.ScreenUtil;
import net.minecraft.util.ChatColor;
import com.adjustclient.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class ChangelogChange {
    public List<ChangelogHighlight> highlights;
    public String prefix;
    public Animation animation;
    public Object field16;

    protected ChangelogChange(String text, String prefix) {
        this.highlights = parse(text);
        this.prefix = prefix;
    }

    public ChangelogHighlight resolveHighlight(String marker) {
        String[] parts = marker.split(":");
        Supplier<Integer> color = () -> -855306;
        if (parts.length == 1) {
            String name = parts[0];
            if (name.length() >= 3 && name.charAt(0) == '/' && name.charAt(2) == '/') {
                if (name.charAt(1) != 'b') throw new RuntimeException("what is fmt " + name.substring(0, 2));
                return new ChangelogHighlight(ChatColor.BOLD + name.substring(3));
            }
            if (name.startsWith("!")) {
                name = name.substring(1);
                Runnable action = null;
                if (name.equalsIgnoreCase("Friends")) {
                    action = () -> Adjust.INSTANCE.screenStack.openFriendsScreen();
                } else if (name.equalsIgnoreCase("Global Configurations")) {
                    action = () -> ClickGui.openGlobalConfigurations();
                } else if (name.equalsIgnoreCase("Detection Status")) {
                    action = () -> {
                        DialogBuilder dialog = new DialogBuilder("Detection Status");
                        dialog.addButton("OK", screen -> { }, screen -> true);
                        dialog.message.append("When a feature from Adjust gets patched on a server, a warning will appear on the main menu letting you know.");
                        dialog.build();
                    };
                } else if (name.equalsIgnoreCase("Player Control")) {
                    action = () -> {
                        DialogBuilder dialog = new DialogBuilder("Player Control");
                        dialog.addButton("OK", screen -> { }, screen -> true);
                        dialog.message.append("In your main hotbar page, you now have a lead item that lets you control your real player only when holding it.");
                        dialog.build();
                    };
                } else if (name.equalsIgnoreCase("Media")) {
                    action = () -> {
                        DialogBuilder dialog = new DialogBuilder("Media");
                        dialog.addButton("OK", screen -> { }, screen -> true);
                        dialog.message.append("If you create content with Adjust, you can get an in-game & Discord rank called Media. More information is available in ");
                        dialog.build();
                    };
                } else if (name.equalsIgnoreCase("Scripting API")) {
                    action = () -> showScriptingDocumentation();
                } else if (name.equalsIgnoreCase("Fernan's Club Integration")) {
                    action = () -> showFernanDocumentation();
                } else if (name.equalsIgnoreCase("Localts Integration")) {
                    action = () -> showLocaltsDocumentation();
                } else if (name.equalsIgnoreCase("Urchin API")) {
                    action = () -> {
                        DialogBuilder dialog = new DialogBuilder("Urchin API");
                        dialog.addButton("OK", screen -> { }, screen -> true);
                        dialog.message.append("Other player's Urchin tags can be shown in the player list .");
                        dialog.build();
                    };
                }
                return new ChangelogHighlight(name, color, Objects.requireNonNull(action, name));
            }
            if (name.startsWith("*")) {
                CommandBase command = Adjust.INSTANCE.commandManager.findCommand(name.substring(1), true);
                Objects.requireNonNull(command, "Command " + name);
                return new ChangelogHighlight(command.getName(), color,
                        () -> showCommandDocumentation(command));
            }
        }
        Module module = (Module) Adjust.INSTANCE.moduleManager.get(parts[0]);
        if (module == null) return new ChangelogHighlight(parts[0]);
        if (parts.length == 1) {
            return new ChangelogHighlight(module.getName(), color,
                    () -> ScreenUtil.openClickGuiAt(module));
        }
        Setting<?> setting = module.findSetting(null, parts[1]);
        if (setting == null) {
            String name = parts[1];
            if (name.contains("->")) name = name.split("->")[1];
            return new ChangelogHighlight(name);
        }
        String name = parts.length == 3 ? parts[2] : setting.getLabel();
        return new ChangelogHighlight(name, color,
                () -> ScreenUtil.openClickGuiAt(setting));
    }

    public static void showCommandDocumentation(CommandBase command) {
        DialogBuilder dialog = new DialogBuilder(command.getName());
        dialog.message.append(String.valueOf(command.field8));
        dialog.addButton("OK", screen -> { }, screen -> true);
        dialog.message.append("\nCommand aliases: ");
        for (int i = 0; i < command.aliases.length; i++) {
            if (i != 0) dialog.message.append(", ");
            dialog.message.bold = true;
            dialog.message.append(command.aliases[i]);
        }
        dialog.build();
    }

    public static void showFernanDocumentation() {
        DialogBuilder dialog = new DialogBuilder("Fernan's Club");
        dialog.addButton("OK", screen -> { }, screen -> true);
        dialog.message.append("Accounts can now be bought from Fernan's Club directly in Adjust client. The shop be accessed from the account manager. ");
        dialog.message.bold = true;
        dialog.message.append("https://fernan.club/api");
        dialog.build();
    }

    public static void showLocaltsDocumentation() {
        DialogBuilder dialog = new DialogBuilder("Localts");
        dialog.addButton("OK", screen -> { }, screen -> true);
        dialog.message.append("Accounts can now be bought from Localts directly in Adjust client. The shop can be accessed from the account manager. More information is available at ");
        dialog.message.bold = true;
        dialog.message.append("https://localts.store/api-docs");
        dialog.build();
    }

    public static void showScriptingDocumentation() {
        DialogBuilder dialog = new DialogBuilder("Scripting API");
        dialog.addButton("OK", screen -> { }, screen -> true);
        dialog.addButton("Documentation", screen -> BrowserUtil.openUrl("https://adjust.club/"),
                screen -> true);
        dialog.message.append("Scripts allow users to create their own features to Adjust client. Read the documentation to get started.");
        dialog.build();
    }

    public List<ChangelogHighlight> parse(String text) {
        List<ChangelogHighlight> result = new ArrayList<>();
        StringBuilder target = new StringBuilder();
        StringBuilder plain = new StringBuilder();
        boolean inside = false;
        boolean escaped = false;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\' && i != chars.length - 1 && chars[i + 1] == '{') {
                escaped = true;
                continue;
            }
            if (c == '{') {
                if (escaped) {
                    (inside ? target : plain).append(c);
                } else {
                    if (plain.length() != 0) {
                        result.add(new ChangelogHighlight(plain.toString()));
                        plain = new StringBuilder();
                    }
                    inside = true;
                }
            } else if (c == '}' && inside) {
                result.add(resolveHighlight(target.toString()));
                target = new StringBuilder();
                inside = false;
            } else {
                (inside ? target : plain).append(c);
            }
        }
        if (plain.length() != 0) result.add(new ChangelogHighlight(plain.toString()));
        return result;
    }
}
