package com.github.saturnvolv.loded.client.event;

import com.github.saturnvolv.loded.client.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;

public class ClientEvents {
    public static void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register((client -> {
            if (client.player != null)
                WaypointHandler.update(client.player);
        }));
    }
}
