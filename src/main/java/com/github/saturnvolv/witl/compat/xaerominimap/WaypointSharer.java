package com.github.saturnvolv.witl.compat.xaerominimap;

import com.github.saturnvolv.witl.client.LodedClient;
import com.github.saturnvolv.witl.client.waypoint.LodestoneWaypoint;
import com.github.saturnvolv.witl.compat.Mods;
import net.minecraft.util.math.random.Random;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.common.settings.ModSettings;

import java.util.Map;

public class WaypointSharer {
    static Boolean MINIMAP_IS_PRESENT = null;
    public static boolean clearWaypoints() {
        if (!enabled()) return false;
        WaypointsManager.getCustomWaypoints(LodedClient.MOD_ID).clear();
        return true;
    }
    public static boolean shareWaypoint( LodestoneWaypoint waypoint ) {
        if (!enabled()) return false;
        Map<Integer, Waypoint> waypointMap = WaypointsManager.getCustomWaypoints(LodedClient.MOD_ID);
        waypointMap.put(waypointMap.size(), toXaeroWaypoint(waypoint));
        return true;
    }
    private static Waypoint toXaeroWaypoint(LodestoneWaypoint waypoint) {
        if (!enabled()) return null;
        Random random = Random.create(waypoint.color());
        return new Waypoint(
                waypoint.position().getX(), waypoint.position().getY(), waypoint.position().getZ(),
                waypoint.name().getString(), "C", random.nextInt(ModSettings.COLORS.length-1)
        );
    }
    public static boolean enabled() {
        if (MINIMAP_IS_PRESENT == null)
            MINIMAP_IS_PRESENT = Mods.XAERO_MINIMAP.isLoaded();
        return MINIMAP_IS_PRESENT;
    }
}
