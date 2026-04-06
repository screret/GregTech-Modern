package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksWaypointHandler;
import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapWaypointHandler;
import com.gregtechceu.gtceu.integration.map.xaeros.XaeroWaypointHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

public class WaypointManager {

    private static ResourceKey<Level> currentDimension;

    @Getter
    private static boolean active = false;

    @ApiStatus.Internal
    public static void init() {
        var compatToggles = ConfigHolder.INSTANCE.compat.minimap.toggle;
        if (compatToggles.xaerosMapIntegration && GTCEu.isModLoaded(GTValues.MODID_XAEROS_MINIMAP)) {
            WaypointManager.registerWaypointHandler(new XaeroWaypointHandler());
            active = true;
        }
        if (compatToggles.journeyMapIntegration && GTCEu.isModLoaded(GTValues.MODID_JOURNEYMAP)) {
            WaypointManager.registerWaypointHandler(new JourneymapWaypointHandler());
            active = true;
        }
        if (compatToggles.ftbChunksIntegration && GTCEu.isModLoaded(GTValues.MODID_FTB_CHUNKS)) {
            WaypointManager.registerWaypointHandler(new FTBChunksWaypointHandler());
            active = true;
        }
    }

    private static final Set<IWaypointHandler> HANDLERS = new HashSet<>();
    private static final Object2ObjectMap<String, WaypointKey> WAYPOINTS = new Object2ObjectArrayMap<>();

    public static void updateDimension(LevelAccessor dim) {
        if (dim instanceof ClientLevel level) {
            currentDimension = level.dimension();
        }
    }

    public static void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, BlockPos pos) {
        if (dim == null) dim = currentDimension;
        for (IWaypointHandler handler : HANDLERS) {
            handler.setWaypoint(key, name, color, dim, pos);
        }
        WAYPOINTS.put(key, new WaypointKey(dim, pos));
    }

    public static void removeWaypoint(String key) {
        for (IWaypointHandler handler : HANDLERS) {
            handler.removeWaypoint(key);
        }
        WAYPOINTS.remove(key);
    }

    public static boolean toggleWaypoint(String key, String name, int color, ResourceKey<Level> dim, BlockPos pos) {
        if (dim == null) dim = currentDimension;
        if (WAYPOINTS.get(key).equals(new WaypointKey(dim, pos))) {
            removeWaypoint(key);
            return false;
        }
        setWaypoint(key, name, color, dim, pos);
        return true;
    }

    public static void registerWaypointHandler(IWaypointHandler handler) {
        HANDLERS.add(handler);
    }

    private record WaypointKey(ResourceKey<Level> dim, BlockPos pos) {}
}
