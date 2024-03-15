package com.github.saturnvolv.witl.client.event;

import com.github.saturnvolv.witl.client.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ClientEvents {
    public static void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register((client -> {
            if (client.player != null)
                WaypointHandler.update(client.player);
        }));
    }
}
