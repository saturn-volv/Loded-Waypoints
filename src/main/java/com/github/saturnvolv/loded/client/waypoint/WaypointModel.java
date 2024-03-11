package com.github.saturnvolv.loded.client.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class WaypointModel {
    private static boolean renderingWorld = false;
    private static final Matrix4f waypointProjection = new Matrix4f();
    private static final Matrix4f waypointModelView = new Matrix4f();

    public static void beforeRenderWorld() {
        renderingWorld = true;
    }

    public static void onResetProjectionMatrix(Matrix4f matrixIn) {
        if (renderingWorld) {
            waypointProjection.identity();
            waypointProjection.mul(matrixIn);
            renderingWorld = false;
        }
    }
    public static void onWorldModelViewMatrix( MatrixStack matrixStack ) {
        waypointModelView.identity();
        waypointModelView.mul(matrixStack.peek().getPositionMatrix());
    }

    public static void onRenderStart() {
        Window mainWindow = MinecraftClient.getInstance().getWindow();
        Matrix4f projectionMatrixBU = RenderSystem.getProjectionMatrix();
        VertexSorter vertexSortingBU = RenderSystem.getVertexSorting();
        Matrix4f ortho = (new Matrix4f()).setOrtho(0.0f, (float) mainWindow.getFramebufferWidth(), (float) mainWindow.getFramebufferHeight(), 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(ortho, VertexSorter.BY_Z);
        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.applyModelViewMatrix();
        WaypointRenderer.getInstance().render(waypointProjection, waypointModelView);
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrixBU, vertexSortingBU);

    }
}
