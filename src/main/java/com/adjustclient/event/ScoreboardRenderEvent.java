package com.adjustclient.event;

import java.util.Collection;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

public final class ScoreboardRenderEvent extends Event {
    public final ScoreObjective objective;
    public final Collection<Score> scores;
    public Overlay overlay;

    public ScoreboardRenderEvent(ScoreObjective objective, Collection<Score> scores) {
        this.objective = objective;
        this.scores = scores;
    }

    public interface Overlay {
        float getWidth();

        void render(ScaledResolution scaledResolution, float width, boolean transparent, float bottomY);
    }
}
