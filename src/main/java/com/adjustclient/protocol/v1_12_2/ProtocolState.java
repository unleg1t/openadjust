package com.adjustclient.protocol.v1_12_2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import com.adjustclient.protocol.v1_12_2.handshake.client.CPacketHandshake;
import com.adjustclient.protocol.v1_12_2.login.client.*;
import com.adjustclient.protocol.v1_12_2.login.server.*;
import com.adjustclient.protocol.v1_12_2.play.client.*;
import com.adjustclient.protocol.v1_12_2.play.server.*;

public enum ProtocolState {
    HANDSHAKING(-1) {
        { registerPacket(ProtocolDirection.SERVERBOUND, CPacketHandshake.class); }
    },
    PLAY(0) {
        {
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnObject.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnExperienceOrb.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnGlobalEntity.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnMob.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnPainting.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnPlayer.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketAnimation.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketStatistics.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketBlockBreakAnim.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUpdateTileEntity.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketBlockAction.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketBlockChange.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUpdateBossInfo.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketServerDifficulty.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketTabComplete.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketChat.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketMultiBlockChange.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketConfirmTransaction.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCloseWindow.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketOpenWindow.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketWindowItems.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketWindowProperty.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSetSlot.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCooldown.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCustomPayload.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCustomSound.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketDisconnect.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityStatus.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketExplosion.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUnloadChunk.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketChangeGameState.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketKeepAlive.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketChunkData.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEffect.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketParticles.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketJoinGame.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketMaps.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntity.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityRelMove.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityLookMove.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityLook.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketMoveVehicle.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSignEditorOpen.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketPlaceGhostRecipe.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketPlayerAbilities.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCombatEvent.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketPlayerListItem.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketPlayerPosLook.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUseBed.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketRecipeBook.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketDestroyEntities.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketRemoveEntityEffect.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketResourcePackSend.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketRespawn.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityHeadLook.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSelectAdvancementsTab.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketWorldBorder.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCamera.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketHeldItemChange.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketDisplayObjective.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityMetadata.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityAttach.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityVelocity.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityEquipment.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSetExperience.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUpdateHealth.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketScoreboardObjective.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSetPassengers.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketTeams.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketUpdateScore.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSpawnPosition.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketTimeUpdate.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketTitle.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketSoundEffect.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketPlayerListHeaderFooter.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketCollectItem.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityTeleport.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketAdvancementInfo.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityProperties.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEntityEffect.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketConfirmTeleport.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketTabComplete.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketChatMessage.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketClientStatus.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketClientSettings.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketConfirmTransaction.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketEnchantItem.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketClickWindow.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketCloseWindow.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketCustomPayload.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketUseEntity.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketKeepAlive.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayer.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerPosition.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerPositionRotation.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerRotation.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketVehicleMove.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketSteerBoat.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlaceRecipe.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerAbilities.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerDigging.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketEntityAction.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketInput.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketRecipeInfo.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketResourcePackStatus.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketSeenAdvancements.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketHeldItemChange.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketCreativeInventoryAction.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketUpdateSign.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketAnimation.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketSpectate.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerTryUseItemOnBlock.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketPlayerTryUseItem.class);
        }
    },
    STATUS(1) {},
    LOGIN(2) {
        {
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketLoginDisconnect.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEncryptionRequest.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketLoginSuccess.class);
            registerPacket(ProtocolDirection.CLIENTBOUND, SPacketEnableCompression.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketLoginStart.class);
            registerPacket(ProtocolDirection.SERVERBOUND, CPacketEncryptionResponse.class);
        }
    };

    private final Map<ProtocolDirection, BiMap<Integer, Class<? extends ProtocolPacket<?>>>> directionMaps = new EnumMap<>(ProtocolDirection.class);
    private final int id;
    public static final ProtocolState[] STATES_BY_ID = new ProtocolState[4];
    private static final Map<Class<? extends ProtocolPacket<?>>, ProtocolState> STATES_BY_CLASS = new HashMap<>();

    ProtocolState(int id) { this.id = id; }

    protected ProtocolState registerPacket(ProtocolDirection direction, Class<? extends ProtocolPacket<?>> packetClass) {
        BiMap<Integer, Class<? extends ProtocolPacket<?>>> packets = directionMaps.get(direction);
        if (packets == null) {
            packets = HashBiMap.create();
            directionMaps.put(direction, packets);
        }
        if (packets.containsValue(packetClass)) {
            throw new IllegalArgumentException(direction + " packet " + packetClass + " is already known to ID " + packets.inverse().get(packetClass));
        }
        packets.put(packets.size(), packetClass);
        return this;
    }

    static {
        for (ProtocolState state : values()) {
            int protocolId = state.id;
            if (protocolId < -1 || protocolId > 2) {
                throw new Error("Invalid protocol ID " + protocolId);
            }
            STATES_BY_ID[protocolId + 1] = state;
            for (ProtocolDirection direction : state.directionMaps.keySet()) {
                for (Class<? extends ProtocolPacket<?>> packetClass : state.directionMaps.get(direction).values()) {
                    if (STATES_BY_CLASS.containsKey(packetClass) && STATES_BY_CLASS.get(packetClass) != state) {
                        throw new Error("Packet " + packetClass + " is already assigned to protocol " + STATES_BY_CLASS.get(packetClass) + " - can't reassign to " + state);
                    }
                    STATES_BY_CLASS.put(packetClass, state);
                    try {
                        packetClass.newInstance();
                    } catch (Exception exception) {
                        throw new Error("Packet " + packetClass + " fails instantiation checks! " + packetClass);
                    }
                }
            }
        }
    }
}
