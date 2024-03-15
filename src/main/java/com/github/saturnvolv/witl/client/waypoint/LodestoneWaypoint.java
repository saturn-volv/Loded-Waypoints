package com.github.saturnvolv.witl.client.waypoint;

import com.github.saturnvolv.witl.client.LodedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import xaero.common.minimap.waypoints.Waypoint;

import java.awt.*;

public class LodestoneWaypoint {
    private final GlobalPos pos;
    private final ItemStack compass;
    private boolean isSelectedItem = false;
    private final boolean failed;
    private int color;

    public LodestoneWaypoint( ItemStack stack ) {
        NbtCompound lodestoneNbt = stack.getNbt();
        if ( lodestoneNbt == null || !CompassItem.hasLodestone(stack))
            throw new IllegalArgumentException("Provide a lodestone targeted compass only.");
        this.compass = stack;
        this.pos = CompassItem.createLodestonePos(lodestoneNbt);
        failed = this.pos == null;
        if (!failed)
            this.color = Random.create(pos.getPos().asLong()).nextInt(0xFFFFFF);
    }

    public BlockPos position() {
        return this.pos.getPos();
    }
    public RegistryKey<World> dimension() {
        return this.pos.getDimension();
    }
    public ItemStack compass() {
        return compass;
    }
    public Text name() {
        return compass.getName();
    }
    public float angle() {
        return angleOfWaypoint(this.position());
    }
    public boolean isSelected() {
        return this.isSelectedItem;
    }
    public void selected( boolean isSelectedItem) {
        this.isSelectedItem = isSelectedItem;
    }
    public boolean failed() {
        return this.failed;
    }
    public int color() {
        return this.color;
    }

    public static float angleOfWaypoint(BlockPos pos) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        assert minecraft.world != null;
        int worldX = pos.getX(); int worldZ = pos.getZ();

        Camera camera = minecraft.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        double deltaX = (double) worldX - cameraPos.getX() + 0.5;
        double deltaZ = (double) worldZ - cameraPos.getZ() + 0.5;

        float Z = (float) (deltaZ == 0.0d ? 0.001f : deltaZ);
        float angle = (float) Math.toDegrees(Math.atan(
           -deltaX / (double) Z
        ));

        if (deltaZ < 0.0) {
            angle += deltaX < 0.0 ? 180 : -180;
        }
        float offset = MathHelper.wrapDegrees(angle - camera.getYaw());
        return Math.abs(offset);
    }
}
