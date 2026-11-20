package com.adjustclient.utilities.lunar.apollo.protocol;

import com.adjustclient.utilities.lunar.apollo.protocol.beam.DisplayBeaconBeamMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.beam.RemoveBeaconBeamMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.beam.ResetBeaconBeamsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.border.DisplayBorderMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.border.RemoveBorderMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.border.ResetBordersMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.chat.DisplayLiveChatMessageMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.chat.RemoveLiveChatMessageMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.coloredfire.OverrideColoredFireMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.coloredfire.ResetColoredFireMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.coloredfire.ResetColoredFiresMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloAdvancedResourceLocationIcon;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloBlockLocation;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloColor;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloComponent;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloCuboid2D;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloCuboid3D;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloEntityId;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloIcon;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloItemStackIcon;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloLocation;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloLunarClientVersion;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloMinecraftVersion;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloPlayerLocation;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloSimpleResourceLocationIcon;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloSimpleResourceLocationSound;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloSound;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoAny;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoDuration;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoTimestamp;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoValue;
import com.adjustclient.utilities.lunar.apollo.protocol.configurable.ApolloConfigurableSettings;
import com.adjustclient.utilities.lunar.apollo.protocol.configurable.OverrideConfigurableSettingsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.cooldown.DisplayCooldownMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.cooldown.RemoveCooldownMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.cooldown.ResetCooldownsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.entity.FlipEntityMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.entity.OverrideRainbowSheepMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.entity.ResetFlipedEntityMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.entity.ResetRainbowSheepMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.CharacterAbilityMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.CloseGuiMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventGameOverviewMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventOverviewMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventPlayerMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventPlayerStatusMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventStatusOverviewMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.EventTeamStatusMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OpenGuiMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideCharacterAbilityMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideCharacterCosmeticMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideCharacterMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideCharacterSuitResourcesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideCosmeticResourcesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.OverrideHeartTextureMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.ResetHeartTextureMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.evnt.UpdateCosmeticsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.glow.OverrideGlowEffectMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.glow.ResetGlowEffectMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.glow.ResetGlowEffectsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.hologram.DisplayHologramMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.hologram.RemoveHologramMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.hologram.ResetHologramsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.limb.HideArmorPiecesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.limb.HideBodyPartMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.limb.ResetArmorPiecesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.limb.ResetBodyPartMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.nametag.OverrideNametagMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.nametag.ResetNametagMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.nametag.ResetNametagsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.nickhider.OverrideNickHiderMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.nickhider.ResetNickHiderMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.notification.DisplayNotificationMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.notification.ResetNotificationsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloBlockHit;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloEntityHit;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloMiss;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloPacketInfo;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloPlayerInfo;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.ApolloRayTraceResult;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.PlayerAttackMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.PlayerChatCloseMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.PlayerChatOpenMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.PlayerUseItemBucketMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment.PlayerUseItemMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.paynow.OpenPayNowEmbeddedCheckoutMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.DisplayPlayerPingMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.RemovePingMarkerTypeMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.RemovePlayerPingMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.RequestPlayerPingMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.ResetPingMarkerTypesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.ResetPlayerPingMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.SetPingMarkerTypeMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.pingmarker.SetPingMarkerTypesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.player.ModMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.player.PlayerHandshakeMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.player.UpdatePlayerWorldMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.richpresence.OverrideServerRichPresenceMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.richpresence.ResetServerRichPresenceMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.staffmod.DisableStaffModsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.staffmod.EnableStaffModsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.stopwatch.ResetStopwatchMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.stopwatch.StartStopwatchMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.stopwatch.StopStopwatchMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.team.ApolloTeamMember;
import com.adjustclient.utilities.lunar.apollo.protocol.team.ResetTeamMembersMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.team.UpdateTeamMembersMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.tebex.OpenTebexEmbeddedCheckoutMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.title.DisplayTitleMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.title.ResetTitlesMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.tntcountdown.SetTntCountdownMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.ApolloPingData;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.ApolloPingRequest;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.ApolloPingResponse;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.ApolloTransferRequest;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.ApolloTransferResponse;
import com.adjustclient.utilities.lunar.apollo.protocol.vignette.DisplayVignetteMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.vignette.ResetVignetteMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.waypoint.DisplayWaypointMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.waypoint.RemoveWaypointMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.waypoint.ResetWaypointsMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ProtoMessageRegistry {
    private static final Map<Class<? extends ProtoMessage>, ProtoMessageType<?>> TYPES;

    static {
        Map<Class<? extends ProtoMessage>, ProtoMessageType<?>> types = new IdentityHashMap<>(122);
        types.put(ResetGlowEffectMessage.class, new ProtoMessageType<>("lunarclient.apollo.glow.v1.ResetGlowEffectMessage",
                bytes -> ResetGlowEffectMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloSimpleResourceLocationIcon.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.SimpleResourceLocationIcon",
                bytes -> ApolloSimpleResourceLocationIcon.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloTeamMember.class, new ProtoMessageType<>("lunarclient.apollo.team.v1.TeamMember",
                bytes -> ApolloTeamMember.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemovePingMarkerTypeMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.RemovePingMarkerTypeMessage",
                bytes -> RemovePingMarkerTypeMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventGameOverviewMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventGameOverviewMessage",
                bytes -> EventGameOverviewMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloEntityId.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.EntityId",
                bytes -> ApolloEntityId.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayCooldownMessage.class, new ProtoMessageType<>("lunarclient.apollo.cooldown.v1.DisplayCooldownMessage",
                bytes -> DisplayCooldownMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloComponent.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Component",
                bytes -> ApolloComponent.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerUseItemBucketMessage.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerUseItemBucketMessage",
                bytes -> PlayerUseItemBucketMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(UpdateCosmeticsMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.UpdateCosmeticsMessage",
                bytes -> UpdateCosmeticsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloLocation.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Location",
                bytes -> ApolloLocation.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloColor.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Color",
                bytes -> ApolloColor.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetStopwatchMessage.class, new ProtoMessageType<>("lunarclient.apollo.stopwatch.v1.ResetStopwatchMessage",
                bytes -> ResetStopwatchMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EnableStaffModsMessage.class, new ProtoMessageType<>("lunarclient.apollo.staffmod.v1.EnableStaffModsMessage",
                bytes -> EnableStaffModsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayLiveChatMessageMessage.class, new ProtoMessageType<>("lunarclient.apollo.chat.v1.DisplayLiveChatMessageMessage",
                bytes -> DisplayLiveChatMessageMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetRainbowSheepMessage.class, new ProtoMessageType<>("lunarclient.apollo.entity.v1.ResetRainbowSheepMessage",
                bytes -> ResetRainbowSheepMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetTeamMembersMessage.class, new ProtoMessageType<>("lunarclient.apollo.team.v1.ResetTeamMembersMessage",
                bytes -> ResetTeamMembersMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(HideArmorPiecesMessage.class, new ProtoMessageType<>("lunarclient.apollo.limb.v1.HideArmorPiecesMessage",
                bytes -> HideArmorPiecesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventPlayerStatusMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventPlayerStatusMessage",
                bytes -> EventPlayerStatusMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideCharacterSuitResourcesMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideCharacterSuitResourcesMessage",
                bytes -> OverrideCharacterSuitResourcesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloLunarClientVersion.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.LunarClientVersion",
                bytes -> ApolloLunarClientVersion.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OpenPayNowEmbeddedCheckoutMessage.class, new ProtoMessageType<>("lunarclient.apollo.paynow.v1.OpenPayNowEmbeddedCheckoutMessage",
                bytes -> OpenPayNowEmbeddedCheckoutMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideCharacterCosmeticMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideCharacterCosmeticMessage",
                bytes -> OverrideCharacterCosmeticMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(StartStopwatchMessage.class, new ProtoMessageType<>("lunarclient.apollo.stopwatch.v1.StartStopwatchMessage",
                bytes -> StartStopwatchMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetGlowEffectsMessage.class, new ProtoMessageType<>("lunarclient.apollo.glow.v1.ResetGlowEffectsMessage",
                bytes -> ResetGlowEffectsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloAdvancedResourceLocationIcon.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.AdvancedResourceLocationIcon",
                bytes -> ApolloAdvancedResourceLocationIcon.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloBlockHit.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.BlockHit",
                bytes -> ApolloBlockHit.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayBorderMessage.class, new ProtoMessageType<>("lunarclient.apollo.border.v1.DisplayBorderMessage",
                bytes -> DisplayBorderMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerHandshakeMessage.class, new ProtoMessageType<>("lunarclient.apollo.player.v1.PlayerHandshakeMessage",
                bytes -> PlayerHandshakeMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetNametagMessage.class, new ProtoMessageType<>("lunarclient.apollo.nametag.v1.ResetNametagMessage",
                bytes -> ResetNametagMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPacketInfo.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PacketInfo",
                bytes -> ApolloPacketInfo.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPingResponse.class, new ProtoMessageType<>("lunarclient.apollo.transfer.v1.PingResponse",
                bytes -> ApolloPingResponse.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetTitlesMessage.class, new ProtoMessageType<>("lunarclient.apollo.title.v1.ResetTitlesMessage",
                bytes -> ResetTitlesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OpenTebexEmbeddedCheckoutMessage.class, new ProtoMessageType<>("lunarclient.apollo.tebex.v1.OpenTebexEmbeddedCheckoutMessage",
                bytes -> OpenTebexEmbeddedCheckoutMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerChatOpenMessage.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerChatOpenMessage",
                bytes -> PlayerChatOpenMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPlayerInfo.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerInfo",
                bytes -> ApolloPlayerInfo.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetNametagsMessage.class, new ProtoMessageType<>("lunarclient.apollo.nametag.v1.ResetNametagsMessage",
                bytes -> ResetNametagsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideCharacterMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideCharacterMessage",
                bytes -> OverrideCharacterMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideRainbowSheepMessage.class, new ProtoMessageType<>("lunarclient.apollo.entity.v1.OverrideRainbowSheepMessage",
                bytes -> OverrideRainbowSheepMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveBeaconBeamMessage.class, new ProtoMessageType<>("lunarclient.apollo.beam.v1.RemoveBeaconBeamMessage",
                bytes -> RemoveBeaconBeamMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayHologramMessage.class, new ProtoMessageType<>("lunarclient.apollo.hologram.v1.DisplayHologramMessage",
                bytes -> DisplayHologramMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloSound.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Sound",
                bytes -> ApolloSound.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPlayerLocation.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.PlayerLocation",
                bytes -> ApolloPlayerLocation.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventOverviewMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventOverviewMessage",
                bytes -> EventOverviewMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetPingMarkerTypesMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.ResetPingMarkerTypesMessage",
                bytes -> ResetPingMarkerTypesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideNametagMessage.class, new ProtoMessageType<>("lunarclient.apollo.nametag.v1.OverrideNametagMessage",
                bytes -> OverrideNametagMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventStatusOverviewMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventStatusOverviewMessage",
                bytes -> EventStatusOverviewMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloMiss.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.Miss",
                bytes -> ApolloMiss.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetColoredFiresMessage.class, new ProtoMessageType<>("lunarclient.apollo.coloredfire.v1.ResetColoredFiresMessage",
                bytes -> ResetColoredFiresMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetCooldownsMessage.class, new ProtoMessageType<>("lunarclient.apollo.cooldown.v1.ResetCooldownsMessage",
                bytes -> ResetCooldownsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloSimpleResourceLocationSound.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.SimpleResourceLocationSound",
                bytes -> ApolloSimpleResourceLocationSound.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ProtoDuration.class, new ProtoMessageType<>("google.protobuf.Duration",
                bytes -> ProtoDuration.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetFlipedEntityMessage.class, new ProtoMessageType<>("lunarclient.apollo.entity.v1.ResetFlipedEntityMessage",
                bytes -> ResetFlipedEntityMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(SetPingMarkerTypeMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.SetPingMarkerTypeMessage",
                bytes -> SetPingMarkerTypeMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloTransferRequest.class, new ProtoMessageType<>("lunarclient.apollo.transfer.v1.TransferRequest",
                bytes -> ApolloTransferRequest.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetBodyPartMessage.class, new ProtoMessageType<>("lunarclient.apollo.limb.v1.ResetBodyPartMessage",
                bytes -> ResetBodyPartMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemovePlayerPingMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.RemovePlayerPingMessage",
                bytes -> RemovePlayerPingMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetBordersMessage.class, new ProtoMessageType<>("lunarclient.apollo.border.v1.ResetBordersMessage",
                bytes -> ResetBordersMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetNotificationsMessage.class, new ProtoMessageType<>("lunarclient.apollo.notification.v1.ResetNotificationsMessage",
                bytes -> ResetNotificationsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloBlockLocation.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.BlockLocation",
                bytes -> ApolloBlockLocation.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetHologramsMessage.class, new ProtoMessageType<>("lunarclient.apollo.hologram.v1.ResetHologramsMessage",
                bytes -> ResetHologramsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayVignetteMessage.class, new ProtoMessageType<>("lunarclient.apollo.vignette.v1.DisplayVignetteMessage",
                bytes -> DisplayVignetteMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPingRequest.class, new ProtoMessageType<>("lunarclient.apollo.transfer.v1.PingRequest",
                bytes -> ApolloPingRequest.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideGlowEffectMessage.class, new ProtoMessageType<>("lunarclient.apollo.glow.v1.OverrideGlowEffectMessage",
                bytes -> OverrideGlowEffectMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ProtoAny.class, new ProtoMessageType<>("google.protobuf.Any",
                bytes -> ProtoAny.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideColoredFireMessage.class, new ProtoMessageType<>("lunarclient.apollo.coloredfire.v1.OverrideColoredFireMessage",
                bytes -> OverrideColoredFireMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetHeartTextureMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.ResetHeartTextureMessage",
                bytes -> ResetHeartTextureMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetVignetteMessage.class, new ProtoMessageType<>("lunarclient.apollo.vignette.v1.ResetVignetteMessage",
                bytes -> ResetVignetteMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveWaypointMessage.class, new ProtoMessageType<>("lunarclient.apollo.waypoint.v1.RemoveWaypointMessage",
                bytes -> RemoveWaypointMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(CharacterAbilityMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.CharacterAbilityMessage",
                bytes -> CharacterAbilityMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayTitleMessage.class, new ProtoMessageType<>("lunarclient.apollo.title.v1.DisplayTitleMessage",
                bytes -> DisplayTitleMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(UpdatePlayerWorldMessage.class, new ProtoMessageType<>("lunarclient.apollo.player.v1.UpdatePlayerWorldMessage",
                bytes -> UpdatePlayerWorldMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetPlayerPingMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.ResetPlayerPingMessage",
                bytes -> ResetPlayerPingMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisableStaffModsMessage.class, new ProtoMessageType<>("lunarclient.apollo.staffmod.v1.DisableStaffModsMessage",
                bytes -> DisableStaffModsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveLiveChatMessageMessage.class, new ProtoMessageType<>("lunarclient.apollo.chat.v1.RemoveLiveChatMessageMessage",
                bytes -> RemoveLiveChatMessageMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RequestPlayerPingMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.RequestPlayerPingMessage",
                bytes -> RequestPlayerPingMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloUuid.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Uuid",
                bytes -> ApolloUuid.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloPingData.class, new ProtoMessageType<>("lunarclient.apollo.transfer.v1.PingData",
                bytes -> ApolloPingData.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloEntityHit.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.EntityHit",
                bytes -> ApolloEntityHit.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideCharacterAbilityMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideCharacterAbilityMessage",
                bytes -> OverrideCharacterAbilityMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideHeartTextureMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideHeartTextureMessage",
                bytes -> OverrideHeartTextureMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveBorderMessage.class, new ProtoMessageType<>("lunarclient.apollo.border.v1.RemoveBorderMessage",
                bytes -> RemoveBorderMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetNickHiderMessage.class, new ProtoMessageType<>("lunarclient.apollo.nickhider.v1.ResetNickHiderMessage",
                bytes -> ResetNickHiderMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloMinecraftVersion.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.MinecraftVersion",
                bytes -> ApolloMinecraftVersion.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideServerRichPresenceMessage.class, new ProtoMessageType<>("lunarclient.apollo.richpresence.v1.OverrideServerRichPresenceMessage",
                bytes -> OverrideServerRichPresenceMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetColoredFireMessage.class, new ProtoMessageType<>("lunarclient.apollo.coloredfire.v1.ResetColoredFireMessage",
                bytes -> ResetColoredFireMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(StopStopwatchMessage.class, new ProtoMessageType<>("lunarclient.apollo.stopwatch.v1.StopStopwatchMessage",
                bytes -> StopStopwatchMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayBeaconBeamMessage.class, new ProtoMessageType<>("lunarclient.apollo.beam.v1.DisplayBeaconBeamMessage",
                bytes -> DisplayBeaconBeamMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(FlipEntityMessage.class, new ProtoMessageType<>("lunarclient.apollo.entity.v1.FlipEntityMessage",
                bytes -> FlipEntityMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ProtoValue.class, new ProtoMessageType<>("google.protobuf.Value",
                bytes -> ProtoValue.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloCuboid2D.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Cuboid2D",
                bytes -> ApolloCuboid2D.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerUseItemMessage.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerUseItemMessage",
                bytes -> PlayerUseItemMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloConfigurableSettings.class, new ProtoMessageType<>("lunarclient.apollo.configurable.v1.ConfigurableSettings",
                bytes -> ApolloConfigurableSettings.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(SetTntCountdownMessage.class, new ProtoMessageType<>("lunarclient.apollo.tntcountdown.v1.SetTntCountdownMessage",
                bytes -> SetTntCountdownMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetArmorPiecesMessage.class, new ProtoMessageType<>("lunarclient.apollo.limb.v1.ResetArmorPiecesMessage",
                bytes -> ResetArmorPiecesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(CloseGuiMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.CloseGuiMessage",
                bytes -> CloseGuiMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloItemStackIcon.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.ItemStackIcon",
                bytes -> ApolloItemStackIcon.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventPlayerMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventPlayerMessage",
                bytes -> EventPlayerMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetBeaconBeamsMessage.class, new ProtoMessageType<>("lunarclient.apollo.beam.v1.ResetBeaconBeamsMessage",
                bytes -> ResetBeaconBeamsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayWaypointMessage.class, new ProtoMessageType<>("lunarclient.apollo.waypoint.v1.DisplayWaypointMessage",
                bytes -> DisplayWaypointMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayNotificationMessage.class, new ProtoMessageType<>("lunarclient.apollo.notification.v1.DisplayNotificationMessage",
                bytes -> DisplayNotificationMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideNickHiderMessage.class, new ProtoMessageType<>("lunarclient.apollo.nickhider.v1.OverrideNickHiderMessage",
                bytes -> OverrideNickHiderMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ModMessage.class, new ProtoMessageType<>("lunarclient.apollo.player.v1.ModMessage",
                bytes -> ModMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideConfigurableSettingsMessage.class, new ProtoMessageType<>("lunarclient.apollo.configurable.v1.OverrideConfigurableSettingsMessage",
                bytes -> OverrideConfigurableSettingsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveHologramMessage.class, new ProtoMessageType<>("lunarclient.apollo.hologram.v1.RemoveHologramMessage",
                bytes -> RemoveHologramMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetServerRichPresenceMessage.class, new ProtoMessageType<>("lunarclient.apollo.richpresence.v1.ResetServerRichPresenceMessage",
                bytes -> ResetServerRichPresenceMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ResetWaypointsMessage.class, new ProtoMessageType<>("lunarclient.apollo.waypoint.v1.ResetWaypointsMessage",
                bytes -> ResetWaypointsMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(SetPingMarkerTypesMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.SetPingMarkerTypesMessage",
                bytes -> SetPingMarkerTypesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerChatCloseMessage.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerChatCloseMessage",
                bytes -> PlayerChatCloseMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloIcon.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Icon",
                bytes -> ApolloIcon.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(RemoveCooldownMessage.class, new ProtoMessageType<>("lunarclient.apollo.cooldown.v1.RemoveCooldownMessage",
                bytes -> RemoveCooldownMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(EventTeamStatusMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.EventTeamStatusMessage",
                bytes -> EventTeamStatusMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloCuboid3D.class, new ProtoMessageType<>("lunarclient.apollo.common.v1.Cuboid3D",
                bytes -> ApolloCuboid3D.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OpenGuiMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OpenGuiMessage",
                bytes -> OpenGuiMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloTransferResponse.class, new ProtoMessageType<>("lunarclient.apollo.transfer.v1.TransferResponse",
                bytes -> ApolloTransferResponse.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ProtoTimestamp.class, new ProtoMessageType<>("google.protobuf.Timestamp",
                bytes -> ProtoTimestamp.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(UpdateTeamMembersMessage.class, new ProtoMessageType<>("lunarclient.apollo.team.v1.UpdateTeamMembersMessage",
                bytes -> UpdateTeamMembersMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(HideBodyPartMessage.class, new ProtoMessageType<>("lunarclient.apollo.limb.v1.HideBodyPartMessage",
                bytes -> HideBodyPartMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(OverrideCosmeticResourcesMessage.class, new ProtoMessageType<>("lunarclient.apollo.evnt.v1.OverrideCosmeticResourcesMessage",
                bytes -> OverrideCosmeticResourcesMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(ApolloRayTraceResult.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.RayTraceResult",
                bytes -> ApolloRayTraceResult.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(PlayerAttackMessage.class, new ProtoMessageType<>("lunarclient.apollo.packetenrichment.v1.PlayerAttackMessage",
                bytes -> PlayerAttackMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        types.put(DisplayPlayerPingMessage.class, new ProtoMessageType<>("lunarclient.apollo.pingmarker.v1.DisplayPlayerPingMessage",
                bytes -> DisplayPlayerPingMessage.parse(new ProtoReader(ByteBuffer.wrap(bytes)))));
        TYPES = Collections.unmodifiableMap(types);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ProtoMessage> ProtoMessageType<T> c(Class<T> messageClass) {
        ProtoMessageType<?> type = TYPES.get(messageClass);
        if (type == null) {
            throw new IllegalArgumentException("Unregistered protobuf message class: " + messageClass.getName());
        }
        return (ProtoMessageType<T>) type;
    }
}
