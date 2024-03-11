package com.github.saturnvolv.loded.client.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class WaypointRenderer {
    private static final WaypointRenderer INSTANCE = new WaypointRenderer();
    private final MinecraftClient CLIENT;

    @Nullable
    private TextRenderer textRenderer;
    private final DrawContext matrixStack;
    private final DrawContext matrixStackOverlay;

    private final WaypointFilter filter = new WaypointFilter();

    private WaypointRenderer() {
        CLIENT = MinecraftClient.getInstance();
        matrixStack = new DrawContext(CLIENT, new BufferBuilderStorage(0).getEntityVertexConsumers());
        matrixStackOverlay = new DrawContext(CLIENT, new BufferBuilderStorage(0).getEntityVertexConsumers());
    }
    public static WaypointRenderer getInstance() {
        return INSTANCE;
    }

    public void render( Matrix4f waypointsProjection, Matrix4f worldModelView ) {
        if (CLIENT.player == null) return;
        this.textRenderer = CLIENT.textRenderer;
        if (textRenderer == null) return;

        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        RenderSystem.disableCull();
        matrixStack.push();
        matrixStack.peek().getPositionMatrix().mul(worldModelView);
        DiffuseLighting.disableGuiDepthLighting();

        matrixStackOverlay.push();

        List<LodestoneWaypoint> waypoints = new ArrayList<>(WaypointHandler.getWaypoints());
        if (!waypoints.isEmpty() && CLIENT.world != null) {
            Entity cameraEntity = CLIENT.getCameraEntity();
            Camera activeRender = CLIENT.gameRenderer.getCamera();
            assert cameraEntity != null;
            Vec3d entityPos = cameraEntity.getPos();
            Vec3d cameraPos = activeRender.getPos();

            Vector3f lookVector = activeRender.getHorizontalPlane().get(new Vector3f());
            this.filter.setParams(lookVector, cameraPos, cameraEntity.getWorld().getRegistryKey());
            Stream<LodestoneWaypoint> waypointStream = waypoints.stream().filter(this.filter);

            double fov = CLIENT.options.getFov().getValue().doubleValue();
            double clampDepth = getWaypointsClampDepth(fov, CLIENT.getWindow().getFramebufferHeight());
            VertexConsumerProvider.Immediate vertexConsumerProvider = this.matrixStack.getVertexConsumers();
            this.renderWaypoints(waypointStream.iterator(), cameraPos, cameraEntity, entityPos, lookVector, clampDepth, vertexConsumerProvider, waypointsProjection);
        }

        matrixStackOverlay.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        DiffuseLighting.enableGuiDepthLighting();
        matrixStack.pop();
    }

    private void renderWaypoints( Iterator<LodestoneWaypoint> iter, Vec3d camPos, Entity entity, Vec3d entityPos, Vector3f lookVector, double clampDepth, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection ) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();
        matrixStackOverlay.translate(0, 0, -2980);

        int count = 0;
        boolean showAllInfo = Screen.hasAltDown();
        while (iter.hasNext()) {
            LodestoneWaypoint waypoint = iter.next();
            this.renderWaypoint(waypoint, lookVector, clampDepth, camPos, entityPos, vertexConsumerProvider, waypointsProjection, waypoint.isSelected(), showAllInfo);
            ++count;
            if (count < 19500)
                matrixStackOverlay.translate(0, 0, 0.1f);
        }
        vertexConsumerProvider.draw();
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
    }

    private void renderWaypoint(LodestoneWaypoint waypoint, Vector3f lookVector, double depthClamp, Vec3d cameraPos, Vec3d entityPos, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection, boolean isSelected, boolean showAllInfo) {
        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        int worldX = waypoint.position().getX();
        int worldY = waypoint.position().getY();
        int worldZ = waypoint.position().getZ();

        double deltaX = (double) worldX - cameraPos.getX() + 0.5;
        double deltaY = (double) worldY - cameraPos.getY() + 1.0;
        double deltaZ = (double) worldZ - cameraPos.getZ() + 0.5;

        double distance2D = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (distance2D >= 0.0d) {
            String name = waypoint.name().getString();
            String distanceTxt = "";

            double depth = deltaX * (double) lookVector.x() + deltaY * (double) lookVector.y() + deltaZ * (double) lookVector.z();
            double correctDeltaX = entityPos.getX() - (double) worldX - 0.5;
            double correctDeltaY = entityPos.getY() - (double) worldY;
            double correctDeltaZ = entityPos.getZ() - (double) worldZ - 0.5;

            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            double correctDistance = Math.sqrt(correctDeltaX * correctDeltaX + correctDeltaY * correctDeltaY + correctDeltaZ * correctDeltaZ);

            if (correctDistance >= 10.0d) {
                boolean couldShowLabels = waypoint.angle() < 10;
                boolean showLabels = couldShowLabels || shouldShowDistance(isSelected, showAllInfo);

                if (showLabels) {
                    if (correctDistance >= 1000)
                        distanceTxt = new DecimalFormat("0.0").format(correctDistance / 1000) + "km";
                    else
                        distanceTxt = new DecimalFormat("0.0").format(correctDistance) + "m";
                } else name = StringUtils.EMPTY;
            }

            matrixStack.push();
            matrixStackOverlay.push();
            if (distance > 250000.0) {
                double cos = 250000 / distance;
                deltaX *= cos; deltaY *= cos; deltaZ *= cos;
            }

            matrixStack.translate(deltaX, deltaY, deltaZ);
            this.drawAsOverlay(waypoint, name, distanceTxt, vertexConsumerProvider, waypointsProjection, depthClamp, depth);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            matrixStack.pop();
            matrixStackOverlay.pop();
        }
    }

    private void drawAsOverlay(LodestoneWaypoint waypoint, String name, String distance, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection, double depthClamp, double depth) {
        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        Vector4f origin4f = new Vector4f(0, 0, 0, 1);
        origin4f.mul(matrixStack.peek().getPositionMatrix());
        origin4f.mul(waypointsProjection);
        int overlayX = (int) ((1 + origin4f.x() / origin4f.w()) / 2 * (float) CLIENT.getWindow().getFramebufferWidth());
        int overlayY = (int) ((1 - origin4f.y() / origin4f.w()) / 2 * (float) CLIENT.getWindow().getFramebufferHeight());
        matrixStackOverlay.translate((float) overlayX, (float) overlayY, 0.0f);
        if (depth < depthClamp) {
            float scale = (float) (depthClamp / depth);
            matrixStackOverlay.scale(scale, scale, scale);
        }
        this.drawIcon(waypoint, name, distance, vertexConsumerProvider);
    }
    private void drawIcon(LodestoneWaypoint waypoint, String name, String distance, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        float iconScale = 2 * (waypoint.isSelected() ? 0.75f : 0.55f);
        double nameScale = 1;
        double distanceScale = 1;
        float halfIconPixel = iconScale / 2;

        matrixStackOverlay.translate( halfIconPixel, 0, 0);
        matrixStackOverlay.scale(iconScale, iconScale, 1.0f);
        RenderSystem.setShaderColor(1, 1, 1, waypoint.isSelected() ? 0.75f : 0.3f);
        this.matrixStackOverlay.fill(-6, -8, 6, 4, 0xFFFFFFFF);
        this.matrixStackOverlay.fill(-4, -6, 4, 2, 0xFFFF00FF);
        //this.matrixStackOverlay.drawItem(waypoint.compass(), -8, -16);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        boolean showName = !name.isEmpty();
        matrixStackOverlay.scale((float) 1 / iconScale, (float) 1/ iconScale, 1);
        matrixStackOverlay.translate(-halfIconPixel, 0, 0);
        matrixStackOverlay.translate(0, 2, 0);
        if (showName) {}
            renderWaypointLabel(name, nameScale, vertexConsumerProvider);
        matrixStackOverlay.translate(0, 2, 0);
        if (!distance.isEmpty()) {}
            renderWaypointLabel(distance, distanceScale, vertexConsumerProvider);
    }
    private void renderWaypointLabel(String label, double labelScale, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();
        assert this.textRenderer != null;
        int nameW = this.textRenderer.getWidth(label);
        int bgW = nameW + 3;
        int halfBgW = bgW / 2;
        int halfNamePixel = 0;
        if ((bgW & 1) != 0) {
            halfNamePixel = (int) labelScale - (int) labelScale / 2;
            matrixStackOverlay.translate((float) (-halfNamePixel), 0,0);
        }

        matrixStackOverlay.scale((float) labelScale, (float) labelScale, 1);
        CLIENT.textRenderer.draw(label, -halfBgW + 2, 1.0f, -1, true, matrixStackOverlay.peek().getPositionMatrix(), vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0x33000000, 15728880);
        matrixStackOverlay.translate(0, 9, 0);
        matrixStackOverlay.scale((float) (1 / labelScale), (float) (1 / labelScale), 1);
        if ((bgW & 1) != 0)
            matrixStackOverlay.translate((float) halfNamePixel, 0, 0);
        RenderSystem.enableBlend();
    }

    private boolean shouldShowDistance(boolean isSelected, boolean showAllInfo) {
        if (isSelected) return true;
        return showAllInfo;
    }

    private static double getWaypointsClampDepth(double fov, int height) {
        int baseIconHeight = 8;
        double worldSizeAtClampDepth = 0.19200003147125244 * (double) height / (double) baseIconHeight;
        double fovMultiplier = 2.0 * Math.tan(Math.toRadians(fov / 2.0));
        return worldSizeAtClampDepth / fovMultiplier;
    }
}
