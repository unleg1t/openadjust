package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.setting.Setting;
import com.adjustclient.setting.SettingContainer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
import com.adjustclient.setting.SettingChangeListener;
import com.adjustclient.ui.tabgui.GuiNode;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import net.minecraft.util.ChatColor;
import com.adjustclient.render.font.Fonts;

public final class ColorComponent extends SettingNode<ColorSetting> implements SettingContainer, KeyPressHandler {
    public static boolean syncingSliders;

    SettingChangeListener<Color> changeListener;
    private ModeSetting<ColorSetting.Mode> typeSetting;
    private final List<Setting<?>> settings;

    public ColorComponent(String label, ColorSetting setting) {
        super(label, setting);
        this.settings = new ArrayList<>(4);
        this.buildChildren();
        this.changeListener = (oldColor, newColor) -> this.syncSliders(setting);
        setting.addListener(this.changeListener);
    }

    @Override
    public boolean keyPressed(int key) {
        if (key == Keyboard.KEY_C) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                String hex = this.getHex();
                com.adjustclient.util.ClipboardUtil.copyToClipboard(hex); // sets the system clipboard
                ChatMessage message = new ChatMessage();
                message.append("Copied ");
                message.color = ChatColor.WHITE;
                message.append(this.label);
                message.color = ChatColor.GRAY;
                message.append(" to your clipboard.");
                ChatUtil.addChatMessage(message.component != null ? message.component : new ChatComponentText(""));
                return true;
            }
        } else if (key == Keyboard.KEY_V) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                String pasted = com.adjustclient.util.ClipboardUtil.getClipboard().replace("#", "").trim(); // clipboard text
                if (pasted.equals(this.getHex())) {
                    return true;
                }
                long value;
                try {
                    value = Long.parseLong(pasted, 16);
                } catch (NumberFormatException e) {
                    ChatMessage error = new ChatMessage();
                    error.append("Invalid hex value.");
                    ChatUtil.addChatMessage(error.component != null ? error.component : new ChatComponentText(""));
                    return true;
                }
                ColorSetting setting = this.setting;
                setting.setValue(new Color((int) value, setting.hasAlpha()));
                ChatMessage message = new ChatMessage();
                message.append("Pasted ");
                message.color = ChatColor.WHITE;
                message.append(pasted);
                message.color = ChatColor.GRAY;
                message.append(" into ");
                message.color = ChatColor.WHITE;
                message.append(this.label);
                message.color = ChatColor.GRAY;
                message.append(".");
                ChatUtil.addChatMessage(message.component != null ? message.component : new ChatComponentText(""));
                return true;
            }
        }
        return false;
    }

    private void syncSliders(ColorSetting colorSetting) {
        boolean hasAlpha = colorSetting.hasAlpha();
        Color color = (Color) colorSetting.getValue();
        float[] hsba = new float[(hasAlpha ? 1 : 0) + 3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsba);
        if (hasAlpha) {
            hsba[3] = color.getAlpha();
        }
        syncingSliders = true;
        if (this.typeSetting != null) {
            this.typeSetting.setValue(colorSetting.getMode());
        }
        int index = 0;
        for (ColorChannel channel : ColorChannel.values()) {
            if (!hasAlpha && channel == ColorChannel.ALPHA) {
                continue;
            }
            // find the NumberComponent row whose first child is the slider of this channel
            for (GuiNode child : this.children) { // D+4: children list
                if (child instanceof NumberComponent row && !row.children.isEmpty()
                        && row.children.get(0) instanceof ColorSliderComponent slider && slider.channel == channel) {
                    slider.setting.setValue(hsba[index]);
                    break;
                }
            }
            index++;
        }
        syncingSliders = false;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        dl.color(this.getTextColor(false, true));
        float textX = x + 2.0F;
        float textY = y + 3.0F;
        dl.text(Fonts.TAHOMA_10, this.label + ": ", textX, textY);
        float swatchX = textX + Fonts.TAHOMA_10.getStringWidth(this.label + ": ");
        int rgb = ((ColorSetting) this.setting).getColor().getRGB() & 0xFFFFFF | 0xFF000000;
        dl.color(rgb);
        dl.rect(swatchX, y + 2.0F, 4.0F, 4.0F);
    }

    private void buildChildren() {
        ColorSetting colorSetting = this.setting;
        ColorSetting.Mode[] modes = colorSetting.disallowMainColor
                ? new ColorSetting.Mode[] {ColorSetting.Mode.PLAIN, ColorSetting.Mode.RAINBOW}
                : new ColorSetting.Mode[] {ColorSetting.Mode.PLAIN, ColorSetting.Mode.RAINBOW, ColorSetting.Mode.MAIN};
        ColorSetting.Mode mode = colorSetting.getMode();
        ModeSetting<ColorSetting.Mode> type = new ModeSetting<>(this, "Type", mode, modes);
        this.typeSetting = type;
        String typeLabel = type.getLabel();
        ModeComponent modeComponent = new ModeComponent(typeLabel, type);
        for (ColorSetting.Mode option : modes) {
            modeComponent.addChild(new ColorModeOptionComponent(this, type, option));
        }
        this.addChild(modeComponent);

        Color color = (Color) colorSetting.getValue();
        boolean hasAlpha = colorSetting.hasAlpha();
        float[] hsba = new float[(hasAlpha ? 1 : 0) + 3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsba);
        if (hasAlpha) {
            hsba[3] = color.getAlpha();
        }
        BooleanSupplier hiddenUnlessPlain = () -> ((ColorSetting) this.setting).getMode() != ColorSetting.Mode.PLAIN;
        for (ColorChannel channel : ColorChannel.values()) {
            if (!hasAlpha && channel == ColorChannel.ALPHA) {
                continue;
            }
            ColorSliderComponent slider = (ColorSliderComponent) channel.factory.createNode(this, channel, hsba);
            NumberSetting sliderSetting = (NumberSetting) slider.setting;
            String sliderLabel = sliderSetting.getLabel();
            NumberComponent row = new NumberComponent(sliderLabel, sliderSetting);
            row.addChild(slider);
            this.addChild(row);
            sliderSetting.hideIf = hiddenUnlessPlain; // Setting+12 dependency / hideIf (BooleanSupplier)
        }
    }

    private String getHex() {
        Color color = (Color) this.setting.getValue();
        return Integer.toHexString(color.getRGB());
    }

    @Override
    public List<Setting<?>> getSettings() {
        return this.settings;
    }
}
