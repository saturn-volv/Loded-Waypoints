package com.github.saturnvolv.loded.mixin;

import com.github.saturnvolv.loded.client.waypoint.WaypointModel;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "loadProjectionMatrix", at = @At("HEAD"))
    private void onLoadProjectionMatrix( Matrix4f projectionMatrix, CallbackInfo ci ) {
        WaypointModel.onResetProjectionMatrix(projectionMatrix);
    }
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorldStart( float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci ) {
        WaypointModel.beforeRenderWorld();
    }
    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD))
    private void onRenderWorldHand( float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci ) {
        WaypointModel.onWorldModelViewMatrix(matrices);
    }
}
