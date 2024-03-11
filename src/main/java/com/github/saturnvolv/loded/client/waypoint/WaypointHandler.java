package com.github.saturnvolv.loded.client.waypoint;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;

import java.util.*;

public class WaypointHandler {
    private static final Map<ItemStack, LodestoneWaypoint> WAYPOINTS = new HashMap<>();
    public static void addWaypoint(LodestoneWaypoint waypoint) {
        WAYPOINTS.put(waypoint.compass(), waypoint);
    }
    public static void removeWaypoint(LodestoneWaypoint waypoint) {
        WAYPOINTS.remove(waypoint.compass());
    }
    public static LodestoneWaypoint getWaypoint(ItemStack itemStack) {
        return WAYPOINTS.getOrDefault(itemStack, new LodestoneWaypoint(itemStack));
    }
    public static Collection<LodestoneWaypoint> getWaypoints() {
        return WAYPOINTS.values();
    }

    public static void update(ClientPlayerEntity user) {
        WAYPOINTS.clear();
        PlayerInventory playerInv = user.getInventory();
        for (int i = 0; i < playerInv.size(); i++) {
            ItemStack stack = playerInv.getStack(i);
            if (CompassItem.hasLodestone(stack)) {
                LodestoneWaypoint waypoint = getWaypoint(stack);
                waypoint.selected(playerInv.selectedSlot == i);
                if (!waypoint.failed())
                    addWaypoint(waypoint);
            }
        }
    }
}
