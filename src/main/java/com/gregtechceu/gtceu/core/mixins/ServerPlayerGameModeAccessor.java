package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerGameModeAccessor {

    @Invoker
    boolean callRemoveBlock(BlockPos pos, boolean canHarvest);
}
