package com.adjustclient.modules.impl.player.guessthebuild;
import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.game.GuessTheBuildGameState;
import com.adjustclient.modules.impl.render.hud.widget.HudWidget;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Vec2f;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.util.ChatColor;
public final class GuessTheBuildHud implements MinecraftInstance {
    public final GuessTheBuildModule module;
    public final Movable movable;
    public final BooleanSetting enabled;
    public final NumberSetting maxThemes;
    public GuessTheBuildHud(GuessTheBuildModule module) {
        this.module = module;
        ExpandSetting group = new ExpandSetting(module, "HUD",
                () -> false);
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        maxThemes = new NumberSetting(group, "Max. themes", 30, 10, 100, 1);
        maxThemes.setDescription("The maximum amount of themes that can be displayed on the HUD");
        group.enabledPredicate = new EnabledCondition(this);
        movable = Adjust.movableManager.registerMovable("guessTheBuild", new Vec2f(2.0F, 200.0F));
        movable.canDrag = () -> isVisible();
    }
    public boolean isVisible() {
        return module.isEnabled() && Adjust.gameStateTracker.state.game == HypixelGame.GUESS_THE_BUILD && enabled.isEnabled();
    }
    public void render(Render2DEvent event, GuessTheBuildGameState state) {
        if (movable.canDrag != null && !movable.canDrag.getAsBoolean()) return;
        List<String> matching = state.matches.stream().limit(maxThemes.getLong()).toList();
        int count = matching.size();
        boolean numbered = count != 0;
        List<String> rows = new ArrayList<>(matching);
        if (!numbered) rows.add("Nothing yet!");
        double widest = rows.stream().mapToDouble(text -> {
            return Fonts.TAHOMA_9.getStringWidth(text);
        }).max().orElse(50.0D);
        float width = (float)Math.max(60.0D, widest + 10.0D);
        Vec2f position = movable.updatePosition();
        movable.width = width;
        movable.height = rows.size() * 6.5F + 9.0F;
        movable.headerHeight = 9.0F;
        HudWidget.drawPanel(140, position.x, position.y, width, rows.size() * 6.5F);
        String title = "Guess The Build " + net.minecraft.util.ChatColor.GRAY + count;
        Fonts.TAHOMA_10.drawString(title, position.x + width * 0.5F - Fonts.TAHOMA_10.getStringWidth(title) * 0.5F,
                position.y + 4.0F, 0xDEF0F0F0);
        float y = position.y + 12.0F;
        int index = 1;
        for (String row : rows) {
            Fonts.TAHOMA_9.drawString(numbered ? index + ". " + row : row, position.x + 2.0F, y, 0xDEF0F0F0);
            ++index;
            y += 6.5F;
        }
    }
    public static final class EnabledCondition implements BooleanSupplier {
        public final GuessTheBuildHud hud;
        public EnabledCondition(GuessTheBuildHud hud) { this.hud = hud; }
        public boolean getAsBoolean() { return hud.enabled.isEnabled(); }
    }
}
