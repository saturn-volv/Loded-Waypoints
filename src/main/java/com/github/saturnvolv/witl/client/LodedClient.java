package com.github.saturnvolv.witl.client;

import com.github.saturnvolv.witl.client.event.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class LodedClient implements ClientModInitializer {
    public static final String MOD_ID = "witl";
    private static LodedClient INSTANCE;
    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        ClientEvents.registerEvents();
    }
    @Nullable
    public static LodedClient getInstance() {
        return INSTANCE;
    }
    @Nullable
    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
    public Random getRandom() {
        if (MinecraftClient.getInstance().world != null)
            return MinecraftClient.getInstance().world.random;
        return Random.createLocal();
    }
}
