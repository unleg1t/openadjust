package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker;

import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.modules.impl.render.hud.widget.HudWidget;
import com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTrackerRenderer.PlayerRow;
import com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTrackerRenderer.TeamRow;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.Vec2f;
import java.util.ArrayList;
import java.util.List;

public final class BedWarsTrackerHud {
    final BedWarsTracker tracker;
    final Movable movable;
    final BooleanSetting render;
    public final List<TeamRow> rows = new ArrayList<>();
    public float width;
    public float height;

    public BedWarsTrackerHud(BedWarsTracker tracker, Movable movable, BooleanSetting render) {
        this.tracker = tracker; this.movable = movable; this.render = render;
    }
    public void draw(Render2DEvent event) {
        if (rows.isEmpty()) return;
        float width = 91.0F;
        float h = 0.0F;
        for (TeamRow r : rows) {
            width = Math.max(width, 2.0F + r.width);
            h += r.players.size() * 8.5F + 18.0F;
        }
        this.width = width;
        this.height = h + 9.0F;
        movable.width = this.width;
        movable.height = this.height;
        movable.headerHeight = 9.0F;
        Vec2f pos = movable.updatePosition();
        movable.drawOverlay(event.scaledResolution);
        HudWidget.drawPanel(140, pos.x, pos.y, width, h);
        Fonts.TAHOMA_10.drawString("Bed Wars Tracker",
                pos.x + width * 0.5F - Fonts.TAHOMA_10.getStringWidth("Bed Wars Tracker") * 0.5F,
                pos.y + 4.0F, 0xDEF0F0F0);
        float y = pos.y + 9.0F + 1.5F;
        for (TeamRow row : rows) {
            BedWarsTrackerRenderer.draw(row, event, pos.x, y);
            y += row.players.size() * 8.5F + 18.0F;
        }
    }
    public void rebuild() {
        rows.clear();
        BedWarsGameState state = (BedWarsGameState) com.adjustclient.Adjust.gameStateTracker.state;
        BedWarsTeam own = state.playerTeams == null ? null
                : state.playerTeams.get(net.minecraft.client.Minecraft.getMinecraft().getSession().getUsername().toLowerCase());
        for (var entry : tracker.teams.entrySet()) {
            if (entry.getKey() == own) continue;
            List<PlayerRow> members = new ArrayList<>();
            for (var roster : state.playerTeams.entrySet()) {
                if (roster.getValue() != entry.getKey()) continue;
                TrackedPlayer player = tracker.getPlayer(roster.getKey());
                if (player != null) members.add(new PlayerRow(player));
            }
            rows.add(new TeamRow(entry.getKey(), entry.getValue(), members));
        }
    }
    public boolean isVisible() { return tracker.isActive() && render.isEnabled() && !rows.isEmpty(); }
}
