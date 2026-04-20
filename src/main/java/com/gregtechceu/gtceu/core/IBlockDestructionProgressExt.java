package com.gregtechceu.gtceu.core;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBlockDestructionProgressExt {

    @Nullable
    List<BlockPos> gtceu$getExtraPositions();

    void gtceu$setExtraPositions(@Nullable List<BlockPos> positions);
}
