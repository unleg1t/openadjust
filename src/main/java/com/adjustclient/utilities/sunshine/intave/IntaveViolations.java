package com.adjustclient.utilities.sunshine.intave;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.placement.PlacementRecord;
import com.adjustclient.utilities.placement.SimplePlacementContext;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatColor;
public final class IntaveViolations {
    public static final Map<Class<?>, String> CHECK_NAMES = new java.util.HashMap<>();
    public final Object2DoubleOpenHashMap<Class<?>> violations=new Object2DoubleOpenHashMap<>();
    public Object field8;
    public void recordViolation(PlacementRecord record) {
        Minecraft mc=Minecraft.getMinecraft();
        if (!mc.isCallingFromMinecraftThread()) {
            mc.addScheduledTask(() -> recordViolation(record));
            return;
        }
        if (record.source==null) {
            ChatUtil.sendClientMessage("Check class missing in Intave violation");
            new Throwable().printStackTrace();
            return;
        }
        Class<?> check=SimplePlacementContext.class;
        Map<Class<?>,String> names=CHECK_NAMES;
        String name=names.get(check);
        if (name==null) { ChatUtil.sendClientMessage("Intave check "+check.getSimpleName()+" is not registered!"); return; }
        if (record.reason==null) {
            ChatUtil.sendClientMessage("Base messages missing in Intave violation for: "+name);
            new Throwable().printStackTrace();
            return;
        }
        double count=violations.getOrDefault(check,0.0)+record.weight;
        ChatMessage message=new ChatMessage();
        message.color=ChatColor.DARK_GRAY; message.bold=true; message.append("[");
        message.color=ChatColor.RED; message.bold=true; message.append("Intave");
        message.color=ChatColor.DARK_GRAY; message.bold=true; message.append("] ");
        message.color=ChatColor.GRAY; message.append("Failed check ");
        ChatMessage tooltip=new ChatMessage();
        tooltip.color=ChatColor.GRAY; tooltip.italic=true; tooltip.append("suspicious block-placement");
        ChatMessage label=new ChatMessage();
        label.color=ChatColor.RED;
        label.hoverEvent=new HoverEvent(HoverEvent.Action.SHOW_TEXT,tooltip.getComponent());
        label.append(name);
        message.append(label.getComponent());
        message.color=ChatColor.GRAY;
        message.append(" x"+RandomUtil.round(count,2));
        if (record.detail!=null) {
            message.color=ChatColor.GRAY; message.italic=true;
            message.append(" ("+"exhibits micro pitch adjustments"+")");
        }
        ChatUtil.addChatMessage(message.getComponent(),0,true,ChatUtil.DEFAULT_COLOR);
        violations.put(check,count);
    }
}
