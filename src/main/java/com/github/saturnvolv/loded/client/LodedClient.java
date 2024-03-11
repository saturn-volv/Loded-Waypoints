package com.github.saturnvolv.loded.client;

import com.github.saturnvolv.loded.client.event.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class LodedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEvents.registerEvents();
    }
    @Nullable
    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
