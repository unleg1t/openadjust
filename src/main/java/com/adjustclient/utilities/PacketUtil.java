package com.adjustclient.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.*;

public final class PacketUtil {
    public static String describe(Packet<?> packet) {
        String name = packet.getClass().getSimpleName();
        Class<?> type = packet instanceof C03PacketPlayer ? C03PacketPlayer.class
                : packet instanceof S14PacketEntity ? S14PacketEntity.class : packet.getClass();
        List<String> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(packet);
                if (value instanceof byte[]) value = Arrays.toString((byte[]) value);
                else if (value instanceof int[]) value = Arrays.toString((int[]) value);
                fields.add(field.getName() + "=" + value);
            } catch (IllegalAccessException ignored) {
            }
        }
        return fields.isEmpty() ? name : name + "[" + String.join(", ", fields) + "]";
    }
    public static boolean isEntityUpdate(Packet<?> packet) {
        Class<?> type = packet.getClass();
        return type == S14PacketEntity.class || type == S14PacketEntity.S15PacketEntityRelMove.class
                || type == S14PacketEntity.S16PacketEntityLook.class
                || type == S14PacketEntity.S17PacketEntityLookMove.class
                || type == S18PacketEntityTeleport.class || type == S19PacketEntityHeadLook.class
                || type == S04PacketEntityEquipment.class;
    }
}
