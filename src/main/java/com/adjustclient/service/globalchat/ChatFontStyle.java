package com.adjustclient.service.globalchat;

import net.minecraft.util.ChatColor;

public enum ChatFontStyle {
    HEART("<3", "§c❤", null),
    STAR(":star:", "§6✮", null),
    YES(":yes:", "§a✔", null),
    NO(":no:", "§c✖", null),
    JAVA(":java:", "§b☕", null),
    ARROW(":arrow:", "§e➜", null),
    SHRUG(":shrug:", "§e¯\\_(ツ)_/¯", null),
    TABLE_FLIP(":tableflip:", "§c(╯°□°）╯§f︵ §7┻━┻", null),
    WAVE("o/", "§d( ﾟ◡ﾟ)/", null),
    _123(":123:", "§a1§e2§c3", null),
    TOTEM(":totem:", "§b☉§e_§b☉", null),
    TYPING(":typing:", "§e✎§6...", null),
    MATHS(":maths:", "§a§l√§e§l(§a§lπ+x§e§l)§a§l=§c§lL", null),
    SNAIL(":snail:", "§e@§a'§e-§a'", null),
    THINKING(":thinking:", "§6(§a0§6.§ao§c?§6)", null),
    GIMME(":gimme:", "§b༼つ◕_◕༽つ", null),
    WIZARD(":wizard:", "§e(§a'§e-§a'§e)⊃§c━§d☆ﾟ.*･｡ﾟ", null),
    PVP(":pvp:", "§e⚔", null),
    PEACE(":peace:", "§a✌", null),
    OOF(":oof:", "§c§lOOF", null),
    PUFFER(":puffer:", "§e§l<('O')>", null),
    DAB(":dab:", "§d<§eo§d/", null),
    SLOTH(":sloth:", "§6(§8・§6⊝§8・§6)", null),
    HAPPY("^_^", "§a^_^", null),
    DOG(":dog:", "§6(ᵔᴥᵔ)", null),
    CUTE(":cute:", "§e(§a✿§e◠‿◠)", null),
    HI("h/", "§eヽ(^◇^*)/", null),
    CAT(":cat:", "§e= §b＾● ⋏ ●＾ §e=", null),
    SNOW(":snow:", "§b☃", null),
    DJ(":dj:", "§9ヽ§5(§d⌐§c■§6_§e■§b)§3ノ§9♬", null),
    YEY(":yey:", "§aヽ (◕◡◕) ﾉ", null),
    HAPPY2("^-^", "§a^-^", null),
    SKULL(":skull:", "§7☠", null),
    ADJUST(":adjust:", "§r§lA§fdjust", null);

    public final String prefix;
    public final String suffix;
    public final String label;

    ChatFontStyle(String prefix, String suffix, String label) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.label = label;
    }

    public static String convert(String text, ChatColor selected) {
        for (ChatFontStyle style : values()) {
            text = text.replace(style.prefix, String.valueOf(style.suffix) + (selected == null ? "null" : selected.toString()));
        }
        return text;
    }
}
