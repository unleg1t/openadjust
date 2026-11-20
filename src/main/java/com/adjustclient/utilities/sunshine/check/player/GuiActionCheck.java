package com.adjustclient.utilities.sunshine.check.player;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.*;
import net.minecraft.network.Packet;
import com.adjustclient.utilities.sunshine.CheckCategory;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import java.util.*;
public class GuiActionCheck extends SunshineCheck implements IncomingPacketListener,OutgoingPacketListener {
    public final List<InventoryState> actions=new ArrayList<>();
    public int openTicks;
    public boolean insideInventory;
    public boolean serverWindow;
    public boolean brandSent;
    public Object field32;
    public GuiActionCheck() { super("Gui Action","Detects impossible actions in screens"); }
    public void recordAction(InventoryState next) {
        InventoryState last=actions.isEmpty()?null:actions.get(actions.size()-1);
        boolean exception=(last==InventoryState.OPEN_INVENTORY && next.tick!=null && next.tick==0)
                || (next==InventoryState.OPEN_INVENTORY && last!=null && last.tick!=null && last.tick==1);
        if (insideInventory && (next.tick==null || next.tick!=openTicks))
            fail(new ViolationDetails(CheckCategory.ALERT).put("last",last).put("openTicks",openTicks).put("insideInventory",next));
        if (last!=null && !exception && (last!=next || !next.repeatable)) {
            if (!last.compatible.contains(next) && !next.compatible.contains(last))
                fail(new ViolationDetails(CheckCategory.ALERT).put("last",last).put("new",next));
        }
        actions.add(next);
    }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer) { tick(); return; }
        if (packet instanceof C0EPacketClickWindow) {
            if (!insideInventory || serverWindow==(((C0EPacketClickWindow)packet).getWindowId()==0))
                fail(new ViolationDetails(CheckCategory.ALERT).put("packet",packet));
        } else if (packet instanceof C0APacketAnimation) recordAction(InventoryState.SWING);
        else if (packet instanceof C15PacketClientSettings) { if (brandSent) recordAction(InventoryState.CLICK_SETTINGS_BUTTON); }
        else if (packet instanceof C0DPacketCloseWindow) {
            if (!insideInventory) fail(new ViolationDetails(CheckCategory.ALERT,"closed inventory when not being in one"));
            serverWindow=false; insideInventory=false; openTicks=0;
            recordAction(InventoryState.CLOSE_INVENTORY);
        } else if (packet instanceof C09PacketHeldItemChange) recordAction(InventoryState.SWITCH_SLOT);
        else if (packet instanceof C16PacketClientStatus) {
            C16PacketClientStatus.EnumState status=((C16PacketClientStatus)packet).getStatus();
            if (status==C16PacketClientStatus.EnumState.PERFORM_RESPAWN) { tick(); serverWindow=false; insideInventory=false; openTicks=0; }
            else if (status==C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) openInventory();
        } else if (packet instanceof C17PacketCustomPayload) {
            if (((C17PacketCustomPayload)packet).getChannelName().equals("MC|Brand")) brandSent=true;
        } else if (packet instanceof C0BPacketEntityAction) {
            switch (((C0BPacketEntityAction)packet).getAction()) {
                case STOP_SNEAKING: recordAction(InventoryState.STOP_SNEAK); break;
                case START_SNEAKING: recordAction(InventoryState.START_SNEAK); break;
                case STOP_SPRINTING: recordAction(InventoryState.STOP_SPRINT); break;
                case START_SPRINTING: recordAction(InventoryState.START_SPRINT); break;
                default: break;
            }
        }
    }
    public void openInventory() {
        if (insideInventory) fail(new ViolationDetails(CheckCategory.ALERT,"opened inventory twice"));
        recordAction(InventoryState.OPEN_INVENTORY);
        serverWindow=false; insideInventory=true; openTicks=0;
    }
    public void tick() { actions.clear(); if (insideInventory) openTicks++; }
    @Override public void onIncomingPacket(Packet<?> packet) {
        if (packet instanceof S01PacketJoinGame) { tick(); serverWindow=false; insideInventory=false; openTicks=0; brandSent=false; }
        else if (packet instanceof S2DPacketOpenWindow) { serverWindow=true; insideInventory=true; openTicks=0; }
        else if (packet instanceof S2EPacketCloseWindow) { serverWindow=false; insideInventory=false; openTicks=0; }
    }
}
