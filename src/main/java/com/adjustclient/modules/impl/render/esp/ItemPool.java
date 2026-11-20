package com.adjustclient.modules.impl.render.esp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ItemPool {
    public final ESPModule owner;
    public final List<com.adjustclient.utilities.anticheat.entity.TrackedItem> source;
    public final com.adjustclient.modules.impl.render.esp.settings.ItemEspSettings settings;

    public ItemPool(ESPModule owner, List<com.adjustclient.utilities.anticheat.entity.TrackedItem> source,
            com.adjustclient.modules.impl.render.esp.settings.ItemEspSettings settings) {
        this.owner = owner;
        this.source = source;
        this.settings = settings;
    }

    public void pool() {
        source.sort(Comparator.comparingInt(item -> item.entityId));
        for (int index = 0; index < source.size(); index++) {
            var first = source.get(index);
            if (first == null) continue;
            source.set(index, null);
            List<com.adjustclient.utilities.anticheat.entity.TrackedItem> group = new ArrayList<>(4);
            group.add(first);
            for (int member = 0; member < group.size(); member++) {
                var entity = group.get(member).getEntity();
                for (int candidate = index + 1; candidate < source.size(); candidate++) {
                    var other = source.get(candidate);
                    if (other != null && entity.getDistanceSqToEntity(other.getEntity()) <= 3.4225) {
                        group.add(other);
                        source.set(candidate, null);
                    }
                }
            }
            if (group.size() == 1) {
                owner.createRenderer(first);
            } else {
                net.minecraft.util.Vec3 center = updateCenter(group);
                owner.renderers.add(new com.adjustclient.modules.impl.render.esp.render.ItemPoolEspRenderer(
                        () -> {
                            return updateCenter(group);
                        }, center, group, owner.theme.getValue(), settings));
            }
        }
    }

    public net.minecraft.util.Vec3 updateCenter(List<com.adjustclient.utilities.anticheat.entity.TrackedItem> group) {
        float partialTicks = net.minecraft.client.Minecraft.getMinecraft().timer.renderPartialTicks;
        var first = group.get(0).getEntity();
        double y = first.lastTickPosY + (first.posY - first.lastTickPosY) * partialTicks;
        double x = 0.0;
        double z = 0.0;
        for (var tracked : group) {
            var entity = tracked.getEntity();
            double itemY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            if (y > itemY) y = itemY;
            x += entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            z += entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        }
        x /= group.size();
        z /= group.size();
        return new net.minecraft.util.Vec3(x == -0.0 ? 0.0 : x, y == -0.0 ? 0.0 : y, z == -0.0 ? 0.0 : z);
    }
}
