package com.adjustclient.module.impl.combat.builder.feature.beddefense;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.BlockPlacedEvent;
import com.adjustclient.event.KeyBindingUpdateEvent;
import com.adjustclient.util.ChatUtil;
import com.adjustclient.util.KeyboardUtil;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import com.adjustclient.util.ChatMessage;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import net.minecraft.util.ChatColor;

public final class BedDefenseRecorder implements Listener, MinecraftInstance {
    // Published by the enclosing Builder factory after recorder construction.
    public static BedDefenseRecorder INSTANCE;
    public final BedDefenseSetting setting;
    public final List<BedDefensePlacement> placements = new ArrayList<>();
    public final Set<BlockPos> recordedPositions = new HashSet<>();
    public BedDefenseStage stage = BedDefenseStage.IDLE;
    public String name;
    public BedAnchor anchor;

    public BedDefenseRecorder(BedDefenseSetting setting) {
        this.setting = setting;
        INSTANCE = this;
    }

    public BedDefenseRecorder(BedDefenseFeature owner, BedDefenseSetting setting) { this(setting); }

    public void startRecording() {
        stage = BedDefenseStage.RECORDING;
        ChatMessage builder = new ChatMessage();
        builder.color = net.minecraft.util.ChatColor.GRAY;
        builder.append("Start placing your defense, then press ");
        IChatComponent instruction = builder.getComponent();
        builder = new ChatMessage();
        builder.bold = true;
        builder.append("Bed Defense ").append(instruction);
        builder.color = net.minecraft.util.ChatColor.WHITE;
        builder.append("Ctrl+R");
        builder.color = net.minecraft.util.ChatColor.GRAY;
        builder.append(" to save it.");
        IChatComponent message = builder.getComponent();
        ChatUtil.addChatMessage(message, 0, true, ChatUtil.DEFAULT_COLOR);
    }

    @EventHandler public void update(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (stage == BedDefenseStage.WAITING_FOR_BED) {
            anchor = BedAnchor.findNearbyBed(mc.thePlayer.getPositionVector());
            if (anchor != null) startRecording();
            return;
        }
        if (stage != BedDefenseStage.RECORDING) return;
        if (mc.thePlayer != null && mc.theWorld != null) {
            BlockPos first = anchor.origin;
            BlockPos second = first.add(anchor.forwardX, 0, anchor.forwardZ);
            if (mc.theWorld.getBlockState(first).getBlock() == Blocks.bed
                    && mc.theWorld.getBlockState(second).getBlock() == Blocks.bed
                    && Math.min(first.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                    second.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) <= 400.0D) return;
        }
        new NotificationBuilder("Bed Defense").addText("Recording interrupted.", 0xFFBEBCBD).show();
        reset();
    }

    @EventHandler public void onPlacement(BlockPlacedEvent event) {
        if (stage != BedDefenseStage.RECORDING) return;
        BlockPos pos = event.pos;
        Block block = event.block;
        if (block == Blocks.bed || anchor.isBedPosition(pos) || recordedPositions.contains(pos)) return;
        if (placements.size() >= 256) {
            new NotificationBuilder("Bed Defense").addText("This defense has reached the placement limit.", 0xFFBEBCBD).show();
            return;
        }
        try {
            placements.add(anchor.toPlacement(pos, block));
            recordedPositions.add(pos);
        } catch (IllegalArgumentException exception) {
            new NotificationBuilder("Bed Defense").addText("That block is too far from the bed to record.", 0xFFBEBCBD).show();
        }
    }

    @EventHandler(priority = 50) public void onKey(KeyBindingUpdateEvent event) {
        if (event.isCancelled() || stage == BedDefenseStage.IDLE) return;
        if (event.keyCode != Keyboard.KEY_R || !KeyboardUtil.isKeyDown()) return;
        event.cancelled = true;
        if (stage == BedDefenseStage.WAITING_FOR_BED) {
            new NotificationBuilder("Bed Defense").addText("Place down a bed before saving the preset.", 0xFFBEBCBD).show();
            return;
        }
        if (placements.isEmpty()) {
            new NotificationBuilder("Bed Defense").addText("Place at least one defense block before saving.", 0xFFBEBCBD).show();
            return;
        }
        int count = placements.size();
        if (setting.addRecordedPreset(name, placements)) {
            new NotificationBuilder("Bed Defense").addText("Saved ", 0xFFBEBCBD).append(name)
                    .addText(" with ", 0xFFBEBCBD).append(Integer.toString(count))
                    .addText(" placements.", 0xFFBEBCBD).show();
        } else {
            new NotificationBuilder("Bed Defense").addText("Could not save the preset in this configuration.", 0xFFBEBCBD).show();
        }
        reset();
    }

    public void beginRecording(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            new NotificationBuilder("Bed Defense").addText("Join a world before recording a defense.", 0xFFBEBCBD).show();
            return;
        }
        reset();
        this.name = name;
        anchor = BedAnchor.findNearbyBed(mc.thePlayer.getPositionVector());
        if (anchor == null) {
            stage = BedDefenseStage.WAITING_FOR_BED;
            new NotificationBuilder("Bed Defense").addText("Place down a bed to start recording your defense.", 0xFFBEBCBD).show();
        } else startRecording();
    }

    public void reset() {
        stage = BedDefenseStage.IDLE;
        name = null;
        anchor = null;
        placements.clear();
        recordedPositions.clear();
    }
}
