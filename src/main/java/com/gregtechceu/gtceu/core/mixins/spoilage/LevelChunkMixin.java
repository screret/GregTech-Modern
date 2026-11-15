package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.common.item.spoilage.BlockEntitySpoilTicker;

import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements ICapabilityProviderImpl<LevelChunk>  {

    @Shadow
    public abstract Level getLevel();

    private LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor,
                            Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sections,
                            @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @SuppressWarnings({ "NameDoesntMatchTargetClass", "ConstantValue", "DataFlowIssue" }) // it's wrong.
    @Inject(method = "unpackTicks", at = @At("TAIL"))
    private void gtceu$unpackBlockEntitySpoilTickers(long gameTime, CallbackInfo ci) {
        // the capability only exists on server levels
        if (this.getLevel().isClientSide) return;

        var holder = this.getCapability(BlockEntitySpoilTicker.ChunkBlockEntityTickHolder.CAPABILITY).orElse(null);
        if (holder == null) return;
        holder.startTickingChunk(gameTime);
    }

    @Inject(method = "*",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;clearRemoved()V"))
    private void gtceu$addBlockEntityToSpoilTickers(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.getLevel() instanceof ServerLevel level) {
            BlockEntitySpoilTicker.get(level).scheduleTick(blockEntity, 0L, TickPriority.EXTREMELY_HIGH);
        }
    }
}
