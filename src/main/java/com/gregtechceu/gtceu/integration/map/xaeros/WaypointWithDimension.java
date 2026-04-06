package com.gregtechceu.gtceu.integration.map.xaeros;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;

public class WaypointWithDimension extends Waypoint {

    private final ResourceKey<Level> dim;

    public WaypointWithDimension(ResourceKey<Level> dim, BlockPos pos,
                                 String name, String symbol, WaypointColor color) {
        super(pos.getX(), pos.getY(), pos.getZ(), name, symbol, color);
        this.dim = dim;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !dim.equals(Minecraft.getInstance().level.dimension());
    }
}
