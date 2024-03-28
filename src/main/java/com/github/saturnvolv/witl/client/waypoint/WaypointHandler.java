package com.github.saturnvolv.witl.client.waypoint;

import com.github.saturnvolv.witl.InventoryStream;
import com.github.saturnvolv.witl.client.LodedClient;
import com.github.saturnvolv.witl.compat.Mods;
import com.github.saturnvolv.witl.compat.xaerominimap.WaypointSharer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;
import xaero.common.core.XaeroMinimapCore;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.*;
import java.util.stream.Stream;

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
        serializeInventoryToWaypoints(InventoryStream.buildStream(playerInv).toList(), playerInv);
    }
    private static void serializeInventoryToWaypoints(List<ItemStack> items, @Nullable Inventory inventory) {
        for (ItemStack stack : items) {
            if (CompassItem.hasLodestone(stack)) {
                LodestoneWaypoint waypoint = getWaypoint(stack);
                if (inventory instanceof PlayerInventory playerInv)
                    waypoint.selected(playerInv.selectedSlot == items.indexOf(stack));
                if (!waypoint.failed())
                    addWaypoint(waypoint);
            } else if (stack.isOf(Items.BUNDLE))
                serializeInventoryToWaypoints(InventoryStream.getBundledStacks(stack).toList(), null);
        }
    }
}
