package com.adjustclient.managers.payload;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePlacement;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePreset;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.managers.party.PartyChatMessage;
import com.adjustclient.core.gui.dialog.ActionLabel;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.ErrorReport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;

public final class BedDefensePresetPayload implements PayloadHandler {
    public static final int PACKET_ID = 3; // PayloadService.handlers: PRESET
    public BedDefensePreset preset;
    public Object field8;

    public boolean handle(ClientUser sender) {
        if (preset == null || BuilderModule.INSTANCE == null) return false;
        com.adjustclient.core.rendering.font.TextComponent[] text = PartyChatMessage.format(new ErrorReport("{} sent you the preset {}.", new Object[]{sender.name, preset.name}));
        PartyChatMessage message = new PartyChatMessage("Bed Defense Preset", text, sender, Integer.MIN_VALUE);
        message.expiresAt = System.currentTimeMillis() + 30000L;
        message.animation = new Animation(Easing.EASE_OUT_SINE, 280L);
        message.accept = new ActionLabel("Accept", Fonts.TAHOMA_10);
        message.decline = new ActionLabel("Decline", Fonts.TAHOMA_10);
        message.accept.action = new Animation(Easing.EASE_OUT_SINE, 300L);
        message.decline.action = new Animation(Easing.EASE_OUT_SINE, 300L);
        message.onDecision = accepted -> onDecision(accepted);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            // PartyService owns the synchronized list addition and adjust:party.message sound (volume .9, pitch .6).
            Adjust.INSTANCE.screenStack.partyService.sendPartyMessage(message);
        });
        return true;
    }

    public void onDecision(Boolean accepted) {
        if (!accepted) return;
        var builder = BuilderModule.INSTANCE;
        if (builder == null || builder.bedDefense == null) return;
        var setting = builder.bedDefense.defenses;
        if (!setting.duplicate(preset)) {
            new NotificationBuilder("Bed Defense").addText("Could not add that preset to the current configuration.", 0xFFBEBCBD).show();
            return;
        }
        String name = preset.name;
        NotificationBuilder notification = new NotificationBuilder("Bed Defense");
        notification.addText("Added ", 0xFFBEBCBD).append(name).addText(" to the current configuration.", 0xFFBEBCBD);
        notification.lifetimeSteps = Math.max(22, (int) (name.length() * 1.3200001F) + 53);
        notification.show();
    }

    public void write(PacketBuffer buffer) {
        buffer.writeByte(1);
        UUID id = preset.id;
        buffer.writeLong(id.getMostSignificantBits());
        buffer.writeLong(id.getLeastSignificantBits());
        buffer.writeString(preset.name);
        List<BedDefensePlacement> placements = preset.placements;
        buffer.writeVarIntToBuffer(placements.size());
        for (BedDefensePlacement placement : placements) {
            buffer.writeByte(placement.x);
            buffer.writeByte(placement.y);
            buffer.writeByte(placement.z);
            buffer.writeByte(placement.type.ordinal());
            buffer.writeShort(placement.blockId);
        }
    }

    public void read(PacketBuffer buffer) {
        if (buffer.readUnsignedByte() != 1) throw new IllegalArgumentException("Unsupported bed defense preset version");
        UUID id = new UUID(buffer.readLong(), buffer.readLong());
        String name = buffer.readStringFromBuffer(32);
        int count = buffer.readVarIntFromBuffer();
        if (count < 1 || count > 256) throw new IllegalArgumentException("Invalid bed defense placement count");
        List<BedDefensePlacement> placements = new ArrayList<>(count);
        Set<Long> positions = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int x = buffer.readByte();
            int y = buffer.readByte();
            int z = buffer.readByte();
            int material = buffer.readUnsignedByte();
            int blockId = buffer.readUnsignedShort();
            if (material >= BedDefenseBlockType.values().length || Block.getBlockById(blockId) == null) {
                throw new IllegalArgumentException("Invalid bed defense material");
            }
            long position = ((long) (x + 16) << 12) | ((long) (y + 16) << 6) | (z + 16);
            if (!positions.add(position)) throw new IllegalArgumentException("Duplicate bed defense position");
            if (Math.abs(x) >= 17 || Math.abs(y) >= 17 || Math.abs(z) >= 17) {
                throw new IllegalArgumentException("Placement is too far from the bed");
            }
            Block block = Block.getBlockById(blockId);
            if (block == null) throw new IllegalArgumentException("Unknown block id: " + blockId);
            placements.add(new BedDefensePlacement(x, y, z, BedDefenseBlockType.values()[material], blockId));
        }
        if (name.isBlank()) throw new IllegalArgumentException("Invalid preset name");
        if (placements.isEmpty() || placements.size() > 256) throw new IllegalArgumentException("Invalid preset placement count");
        preset = new BedDefensePreset(id, null, name, false, List.copyOf(placements));
    }
}
