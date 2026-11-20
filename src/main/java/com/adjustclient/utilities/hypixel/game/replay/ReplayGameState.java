package com.adjustclient.utilities.hypixel.game.replay;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedFakePlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.util.StringUtils;
import net.minecraft.init.Items;

public class ReplayGameState extends HypixelGameState {
    public Integer subjectEntityId;
    public Object field48;
    public ReplayGameState() { this(HypixelGame.REPLAY); }
    protected ReplayGameState(HypixelGame game) { super(game); }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before && packet instanceof S13PacketDestroyEntities && subjectEntityId != null) {
            for (int entityId : ((S13PacketDestroyEntities) packet).getEntityIDs()) {
                if (subjectEntityId == entityId) { subjectEntityId = null; break; }
            }
        }
    }

    @Override public boolean isSpectator() { return true; }

    @Override public void onUpdate() {
        if (game == HypixelGame.REPLAY) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[7];
            if (item != null && item.getItem() == Items.ghast_tear) {
                current(HypixelGame.ATLAS.stateFactory.create());
                return;
            }
        }
        if (subjectEntityId != null) return;
        for (TrackedFakePlayer player : ServerTracker.INSTANCE.entityTracker.getEntities(34, TrackedFakePlayer.class)) {
            String name = StringUtils.stripControlCodes(player.getDisplayName()).trim();
            String subject = game == HypixelGame.ATLAS ? "Suspect" : mc.getSession().getUsername();
            if (name.equals(subject)) {
                subjectEntityId = player.entityId;
                return;
            }
        }
    }
}
