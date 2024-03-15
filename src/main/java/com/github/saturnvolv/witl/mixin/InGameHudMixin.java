package com.github.saturnvolv.witl.mixin;

import com.github.saturnvolv.witl.client.waypoint.WaypointModel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart( DrawContext context, float tickDelta, CallbackInfo ci ) {
        WaypointModel.onRenderStart();
    }
}
