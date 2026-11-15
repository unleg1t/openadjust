package com.adjustclient.ui.config;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.*;
import com.adjustclient.ui.dialog.DropdownElement;
import com.adjustclient.util.Vec2f;
import net.minecraft.client.gui.ScaledResolution;
import java.util.*;
import java.util.stream.Collectors;

final class GlobalConfigList {
    public final List<GlobalConfigEntry> entries = new ArrayList<>();
    public String title;
    public DropdownElement sortDropdown;
    public TextInputBox search;
    public GlobalConfigsScreen screen;
    public Animation scrollAnimation;
    public float scroll;
    public boolean showCount;
    private Object field36;

    public GlobalConfigList(GlobalConfigsScreen screen) {
        sortDropdown = new DropdownElement("Sort by...", Arrays.stream(GlobalConfigSort.values())
                .map(sort -> sort.displayName)
                .toArray(String[]::new));
        search = new TextInputBox(new TextInput("Search...", Fonts.TAHOMA_12, 12));
        scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 150L);
        this.screen = screen;
        title = "Featured";
        sortDropdown.selectionListener = (oldValue, newValue) -> {
            sort(newValue == -1 ? null : GlobalConfigSort.values()[newValue]);
        };
    }

    public void sort(GlobalConfigSort sort) {
        List<GlobalConfigEntry> featured = new ArrayList<>();
        List<GlobalConfigEntry> remaining = new ArrayList<>();
        if (sort == null) {
            for (GlobalConfigEntry entry : entries) {
                (entry.featured ? featured : remaining).add(entry);
            }
            featured.sort(Comparator.comparingInt((GlobalConfigEntry entry) -> entry.id));
        } else remaining.addAll(entries);
        remaining.sort(sort == null ? Comparator.comparingInt((GlobalConfigEntry entry) -> entry.id) : sort.comparator);
        scroll = 0.0F;
        entries.clear();
        entries.addAll(featured);
        entries.addAll(remaining);
    }

    public static boolean matches(String query, GlobalConfigEntry entry) {
        return entry.name.toLowerCase().contains(query)
                || entry.description.toLowerCase().contains(query)
                || entry.author.name.toLowerCase().contains(query);
    }

    public float draw(Vec2f mouse, ScaledResolution window, float x, float y) {
        float width = screen.width;
        String query = search.input.field.getText().toLowerCase();
        List<GlobalConfigEntry> visible = query.isEmpty() ? entries : entries.stream()
                .filter(entry -> matches(query, entry)).collect(Collectors.toList());
        if (showCount) {
            String text = entries.size() + " configuration" + (entries.size() == 1 ? "" : "s");
            if (visible.size() != entries.size()) text += " (" + visible.size() + " showing)";
            Fonts.TAHOMA_12.drawCenteredString(text, x + width * 0.5F, y + 5.0F, 0xE4E1E4E4);
        }
        scrollAnimation.update(scroll);
        float cardX = x + 4.0F;
        float cardY = y + 14.5F - scrollAnimation.value;
        boolean dropdownHovered = sortDropdown.isHovered(mouse, sortDropdown.expanded);
        RenderUtil.pushScissor(window, 1, screen.position.x, screen.position.y + 25.5F, width, screen.height - 25.5F);
        float alpha = com.adjustclient.render.font.image.NvgDraw.getGlobalAlpha();
        Vec2f cardMouse = dropdownHovered ? new Vec2f(-999.0F, -999.0F) : mouse;
        for (int i = 0; i < visible.size(); i++) {
            GlobalConfigEntry entry = visible.get(i);
            if (entry.appearAnimation == null && (i == 0 || (visible.get(i - 1).appearAnimation == null ? 0.0F
                    : visible.get(i - 1).appearAnimation.value) > 0.37F - Math.min(0.21F, i * 0.018F))) {
                entry.appearAnimation = new Animation(Easing.LINEAR, 110L);
            }
            com.adjustclient.render.font.image.NvgDraw.setGlobalAlpha(Math.min(alpha, entry.appearAnimation == null ? 0.0F : entry.appearAnimation.value));
            entry.draw(cardMouse, window, cardX, cardY);
            cardX += 123.0F;
            if (cardX > x + width - 3.0F && i != entries.size() - 1) {
                cardX = x + 4.0F;
                cardY += 38.0F;
            }
        }
        com.adjustclient.render.font.image.NvgDraw.setGlobalAlpha(alpha);
        RenderUtil.popScissor();
        search.input.setWidth(80);
        search.width = 80.0F;
        com.adjustclient.ui.hud.layout.Point2f inputMouse = new com.adjustclient.ui.hud.layout.Point2f();
        inputMouse.x = mouse.x;
        inputMouse.y = mouse.y;
        search.draw(window, inputMouse, x + width - 84.5F, y + 2.5F);
        sortDropdown.draw(window, mouse, x + 4.5F, y + 2.5F, 70.0F);
        return cardY + 35.0F - y;
    }

    public void addAll(List<GlobalConfigEntry> incoming) {
        for (GlobalConfigEntry entry : incoming) entry.screen = screen;
        entries.addAll(incoming);
        Object selected = sortDropdown.selected;
        int index = sortDropdown.options.indexOf(selected);
        sort(index == -1 ? null : GlobalConfigSort.values()[index]);
    }
}
