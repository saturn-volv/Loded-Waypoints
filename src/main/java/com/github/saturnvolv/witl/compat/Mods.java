package com.github.saturnvolv.witl.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.function.Supplier;

public enum Mods {
    XAERO_MINIMAP("xaerominimap");

    private final String id;
    Mods(String id) {
        this.id = id;
    }
    public String asId() {
        return id;
    }

    public boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(asId());
    }

    public <T> Optional<T> runIfInstalled( Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    public void executeIfInstalled(Supplier<Runnable> toExecute) {
        if (isLoaded())
            toExecute.get().run();
    }
}
