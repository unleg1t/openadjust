package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.module.impl.render.esp.EspPlayerFlag;
import com.adjustclient.setting.*;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

public class PlayerEspSettings extends MobEspSettings {
    public final ExpandSetting playerFlags;
    public final BooleanSetting playerFlagsEnabled;
    public final Map<EspPlayerFlag, EspPlayerFlagGroup> flagGroups = new java.util.HashMap<>();
    public final BooleanSetting adjustName;
    public final BooleanSetting teamColor;
    public final BooleanSetting borderTeamColor;
    public final ExpandSetting hackingGroup;
    public final BooleanSetting playerHacking;
    public final ModeSetting<EspIconLocation> playerHackingLocation;
    public PlayerEspSettings(SettingContainer parent, String name, boolean enabled) {
        super(parent, name, enabled, true);
        playerFlags = new ExpandSetting(target, "Player flags"); playerFlagsEnabled = new BooleanSetting(playerFlags, "Enabled", true, "Yes", "No");
        playerFlags.enabledPredicate = () -> playerFlagsEnabled.isEnabled();
        addFlag(EspPlayerFlag.WHITELISTED, "Whitelisted", new Color(70, 70, 200)); addFlag(EspPlayerFlag.TARGETED, "Targeted", new Color(160, 40, 40));
        addFlag(EspPlayerFlag.HACKER, "Hacker", new Color(200, 70, 70)); addFlag(EspPlayerFlag.MURDERER, "Murderer", new Color(208, 67, 67));
        addFlag(EspPlayerFlag.DETECTIVE, "Detective", new Color(26, 86, 154)); addFlag(EspPlayerFlag.AUTO_BLOCK, "Auto block", new Color(255, 217, 99));
        addFlag(EspPlayerFlag.CREATIVE_MODE, "Creative Mode", new Color(149, 59, 190)); addFlag(EspPlayerFlag.TALKING, "Talking", new Color(60, 222, 87));
        addFlag(EspPlayerFlag.BLOCKING, "Blocking", new Color(233, 235, 234)); addFlag(EspPlayerFlag.SNEAKING, "Sneaking", new Color(233, 235, 234));
        adjustName = new BooleanSetting(nametags, "Adjust name", true);
        teamColor = new BooleanSetting(nametags, "Team color", true, () -> customName.isEnabled());
        int colorIndex = nametags.getSettings().indexOf(nametagColor);
        nametags.getSettings().remove(nametagColor);
        nametagColor = new ColorSetting(nametags, "Color", new Color(233, 235, 234), false, false, ColorSetting.Mode.PLAIN,
                () -> teamColor.isEnabled());
        nametags.getSettings().remove(nametagColor);
        nametags.getSettings().add(colorIndex, nametagColor);
        hackingGroup = new ExpandSetting(icons, "Hacking"); playerHacking = new BooleanSetting(hackingGroup, "Enabled", true, "Yes", "No");
        hackingGroup.enabledPredicate = () -> playerHacking.isEnabled();
        playerHackingLocation = new ModeSetting<>(hackingGroup, "Location", EspIconLocation.NAMETAG, EspIconLocation.values());
        java.util.function.BooleanSupplier previousIconsEnabled = icons.enabledPredicate;
        icons.enabledPredicate = () -> previousIconsEnabled.getAsBoolean() || playerHacking.isEnabled();
        icons.getSettings().remove(hackingGroup);
        icons.getSettings().add(2, hackingGroup);
        borderTeamColor = new BooleanSetting(border, "Team color", false);
    }
    private void addFlag(EspPlayerFlag flag, String name, Color color) {
        boolean enabled = flag != EspPlayerFlag.BLOCKING && flag != EspPlayerFlag.SNEAKING;
        flagGroups.put(flag, new EspPlayerFlagGroup(playerFlags, name, color, enabled));
    }
    @Override public String getName(com.adjustclient.util.anticheat.entity.TrackedEntity<?> entity) {
        String name = super.getName(entity);
        if (com.adjustclient.module.impl.render.streamer.StreamerModule.nameMap == null) return name;
        return com.adjustclient.module.impl.render.streamer.StreamerModule.replaceName(name, entity.getName());
    }
}
