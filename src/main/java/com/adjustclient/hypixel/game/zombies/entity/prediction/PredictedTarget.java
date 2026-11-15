package com.adjustclient.hypixel.game.zombies.entity.prediction;

import com.adjustclient.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.hypixel.game.zombies.entity.TrackedEntityRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;

public final class PredictedTarget extends TrackedEntityRef {
    public final PositionPredictor predictor;
    public PredictionMode type;
    public Object field20;
    public PredictedTarget(ZombiesGameState state, int id, int entityType) {
        super(id); this.state = state;
        type = entityType == 61 ? PredictionMode.BLAZE
            : entityType == 53 ? PredictionMode.GIANT : PredictionMode.REGULAR;
        predictor = new PositionPredictor(this);
    }
    @Override public void update() {
        Entity entity = getEntity();
        if (!(entity instanceof EntityLivingBase living)) return;
        if (type == PredictionMode.OLD_ONE || type == PredictionMode.WORLD_ENDER
                || type == PredictionMode.BOSS || state.map == null) return;
        int round = state.round;
        switch (state.map) {
            case DEAD_END, PRISON -> {
                if ((round == 10 || round == 20 || round == 30) && living.getMaxHealth() >= 100.0F)
                    type = PredictionMode.BOSS;
            }
            case ALIEN_ARCADIUM -> {
                if (round == 25 || round == 35) type = PredictionMode.BOSS;
                else if (round == 101) type = PredictionMode.WORLD_ENDER;
                else if (round >= 40 && living.isChild() && living.getMaxHealth() > 200.0F) {
                    type = PredictionMode.OLD_ONE;
                    if (state.isNewEntity(living.getEntityId())) state.onSpecialBoss();
                }
            }
            default -> { }
        }
    }
    @Override public boolean isExpired() {
        Entity entity = getEntity();
        if (entity instanceof EntityLivingBase living && (living.isDead || living.getHealth() <= 0)) return true;
        if (entity instanceof EntityPlayer player) {
            Team team = player.getTeam();
            return team != null && ("Team".equals(team.getRegisteredName()) || "Players".equals(team.getRegisteredName()));
        }
        return false;
    }
}
