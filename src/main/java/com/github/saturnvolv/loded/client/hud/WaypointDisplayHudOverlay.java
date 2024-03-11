package com.github.saturnvolv.loded.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WaypointDisplayHudOverlay implements HudRenderCallback {
    private final MinecraftClient client;
    public WaypointDisplayHudOverlay( ) {
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public void onHudRender( DrawContext drawContext, float tickDelta ) {
        int windowWidth = client.getWindow().getWidth();
        int windowHeight = client.getWindow().getHeight();
        int guiScale = client.options.getGuiScale().getValue();
        if (client.player != null) {
            PlayerInventory inventory = client.player.getInventory();
            List<ItemStack> lodestoneCompasses = new ArrayList<>();
            for ( int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty() && CompassItem.hasLodestone(stack))
                    lodestoneCompasses.add(stack);
            }

            
        }
    }
}
