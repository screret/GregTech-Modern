package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.IBlockDestructionProgressExt;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(BlockDestructionProgress.class)
public class BlockDestructionProgressMixin implements IBlockDestructionProgressExt {

    @Unique
    private List<BlockPos> gtceu$extraPositions;

    @Override
    public List<BlockPos> gtceu$getExtraPositions() {
        return gtceu$extraPositions;
    }

    @Override
    public void gtceu$setExtraPositions(List<BlockPos> positions) {
        gtceu$extraPositions = positions;
    }
}
