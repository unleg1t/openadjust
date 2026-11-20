package com.adjustclient.managers.user;

import com.adjustclient.core.settings.ModeOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.util.ResourceLocation;

public enum Cape implements ModeOption {
    NONE("None", null, 0, false, false),
    ADJUST("Adjust", "textures/cape/adjust.png", 1, false, true),
    NOSTALGIA("Minecraft", "textures/cape/nostalgia.png", 2, false, true),
    CUSTOM("Custom", null, 3, true, false);

    private final String displayName;
    private final String texturePath;
    public ResourceLocation texture;
    public final int id;
    public final boolean field32;
    public final boolean field33;
    private Object field36;
    public static final List<Cape> OPTIONS = Arrays.asList(values());
    public static final Map<Integer, Cape> BY_ID = OPTIONS.stream().collect(Collectors.toMap(
            cape -> cape.id,
            Function.identity()));

    Cape(String displayName, String texturePath, int id, boolean field32, boolean field33) {
        this.displayName = displayName;
        this.texturePath = texturePath;
        this.id = id;
        this.field32 = field32;
        this.field33 = field33;
    }

    @Override public String getName() { return displayName; }

    public static void initializeTextures() {
        for (Cape cape : values()) {
            if (cape.texturePath != null) cape.texture = new ResourceLocation("adjust", cape.texturePath);
        }
    }

}
