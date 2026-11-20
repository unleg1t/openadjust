package com.adjustclient.modules.impl.render.hud.playerlist.menu;

import com.adjustclient.modules.impl.render.hud.playerlist.HoverCardState;
import com.adjustclient.core.rendering.font.Fonts;
import net.minecraft.util.ChatColor;
import com.adjustclient.modules.impl.exploit.playerdetect.hackerdetect.data.TrackedPlayer;
import com.adjustclient.modules.impl.exploit.playerdetect.hackerdetect.check.Check;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;

public final class ViolationsMenu extends HoverMenu {

    public TrackedPlayer violations;

    public ViolationsMenu(HoverCardState owner, TrackedPlayer violations) {
        this.owner = owner;
        this.violations = violations;
        this.item = new MenuItem("Back", (entry, tracked) -> {
            PlayerContextMenu menu = new PlayerContextMenu(owner);
            menu.build();
            owner.menu = menu;
        });
    }

    /** Total height: 14.5 for an empty list, else 5.5 per entry + 6.5 + 2.5. */
    @Override
    public float layout(float x, float y) {
        float height;
        int count = this.violations == null ? 0 : this.violations.violations.size();
        if (count != 0) {
            height = count * 5.5F + 6.5F + 2.5F;
        } else {
            height = 14.5F;
        }
        this.item.y = y + (height - 8.0F);
        return height;
    }

    public void render(float x, float y, float width, float alpha) {
        render(x, y, width, -111.0F, -111.0F, alpha);
    }

    @Override
    public void render(float x, float y, float width, float mouseX, float mouseY, float alpha) {
        int textColor = ((int) (alpha * 200.0F) << 24) | 0xE9E9E9;
        Fonts.TAHOMA_10.drawString("Violations", x, y, ((int) (alpha * 255.0F) << 24) | 0xF4F4F4);
        float cy = y + 6.5F;
        Object2FloatMap<Check> map = this.violations == null ? null : this.violations.violations;
        if (map == null || map.size() == 0) {
            Fonts.TAHOMA_8.drawString("No violations found.", x, cy, textColor);
        } else {
            for (Object2FloatMap.Entry<Check> entry : map.object2FloatEntrySet()) {
                Check check = entry.getKey();
                int level = (int) entry.getFloatValue();
                ChatColor colour = check.getViolationThreshold() > level ? ChatColor.WHITE : ChatColor.RED;
                String line = colour + check.name + " " + ChatColor.RESET + level + "x";
                Fonts.TAHOMA_8.drawString(line, x, cy, textColor);
                cy += 5.5F;
            }
        }
        this.item.x = (x + width) - this.item.width - 5.0F;
        this.item.render(mouseX, mouseY, alpha);
    }
}
