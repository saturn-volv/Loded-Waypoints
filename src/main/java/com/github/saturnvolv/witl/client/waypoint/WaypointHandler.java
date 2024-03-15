package com.github.saturnvolv.witl.client.waypoint;

import com.github.saturnvolv.witl.client.LodedClient;
import com.github.saturnvolv.witl.compat.Mods;
import com.github.saturnvolv.witl.compat.xaerominimap.WaypointSharer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import xaero.common.core.XaeroMinimapCore;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.*;

public class WaypointHandler {
    private static final Map<ItemStack, LodestoneWaypoint> WAYPOINTS = new HashMap<>();
    public static void addWaypoint(LodestoneWaypoint waypoint) {
        Optional<Boolean> shareIfPresent = Mods.XAERO_MINIMAP.runIfInstalled(() -> () -> WaypointSharer.shareWaypoint(waypoint));
        if (shareIfPresent.isEmpty() || !shareIfPresent.get())
            WAYPOINTS.put(waypoint.compass(), waypoint);
    }
    private static void clearWaypoints() {
        Optional<Boolean> clearIfPresent = Mods.XAERO_MINIMAP.runIfInstalled(() -> WaypointSharer::clearWaypoints);
        if (clearIfPresent.isEmpty() || !clearIfPresent.get())
            WAYPOINTS.clear();
    }
    public static LodestoneWaypoint getWaypoint(ItemStack itemStack) {
        return WAYPOINTS.getOrDefault(itemStack, new LodestoneWaypoint(itemStack));
    }
    public static Collection<LodestoneWaypoint> getWaypoints() {
        return WAYPOINTS.values();
    }

    public static void update(ClientPlayerEntity user) {
        clearWaypoints();
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
