package com.github.saturnvolv.loded.client.waypoint;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.function.Predicate;

public class WaypointFilter implements Predicate<LodestoneWaypoint> {
    private Vector3f lookVector;
    private Vec3d cameraPos;
    private RegistryKey<World> dimension;
    public void setParams( Vector3f lookVector, Vec3d cameraPos, RegistryKey<World> dimension) {
        this.lookVector = lookVector;
        this.cameraPos = cameraPos;
        this.dimension = dimension;
    }

    @Override
    public boolean test( LodestoneWaypoint waypoint ) {
        if (!waypoint.dimension().equals(this.dimension))
            return false;
        double deltaX = waypoint.position().getX() - this.cameraPos.getX() + 0.5;
        double deltaY = waypoint.position().getY() - this.cameraPos.getY() + 1.0;
        double deltaZ = waypoint.position().getZ() - this.cameraPos.getZ() + 0.5;

        double depth =
                deltaX * (double) this.lookVector.x() +
                deltaY * (double) this.lookVector.y() +
                deltaZ * (double) this.lookVector.z();
        if (depth <= 0.1) return false;
        double dist2D = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return dist2D >= 0;
    }
}
