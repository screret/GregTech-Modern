package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.IBlockDestructionProgressExt;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = LevelRenderer.class, priority = 500)
@OnlyIn(Dist.CLIENT)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    private ClientLevel level;

    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/level/BlockDestructionProgress;updateTick(I)V",
                     shift = At.Shift.AFTER))
    private void gtceu$addAOEBreakProgress(int breakerId, BlockPos pos, int progress,
                                           CallbackInfo ci,
                                           @Local BlockDestructionProgress progressObj) {
        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (minecraft.player.isShiftKeyDown() || !ToolHelper.hasBehaviorsTag(mainHandItem) ||
                !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(mainHandItem);
        if (aoeDefinition.isZero()) return;

        BlockState state = level.getBlockState(pos);
        if (!mainHandItem.isCorrectToolForDrops(state)) return;

        UseOnContext context = new UseOnContext(minecraft.player, InteractionHand.MAIN_HAND, hitResult);
        List<BlockPos> extraPositions = ToolHelper.getHarvestableBlocks(aoeDefinition, context);

        extraPositions.remove(pos);
        ((IBlockDestructionProgressExt) progressObj).gtceu$setExtraPositions(extraPositions);
        for (BlockPos extraPos : extraPositions) {
            destructionProgress.computeIfAbsent(extraPos.asLong(), l -> Sets.newTreeSet()).add(progressObj);
        }
    }

    @Inject(method = "removeProgress(Lnet/minecraft/server/level/BlockDestructionProgress;)V", at = @At("RETURN"))
    private void gtceu$removeAOEBreakProgress(BlockDestructionProgress progress, CallbackInfo ci) {
        List<BlockPos> extraPositions = ((IBlockDestructionProgressExt) progress).gtceu$getExtraPositions();
        if (extraPositions == null) {
            return;
        }
        for (BlockPos pos : extraPositions) {
            long packed = pos.asLong();
            Set<BlockDestructionProgress> set = destructionProgress.get(packed);
            if (set != null) {
                set.remove(progress);

                if (set.isEmpty()) {
                    destructionProgress.remove(packed);
                }
            }
        }
    }

    @Shadow
    private static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                    double x, double y, double z,
                                    float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @Unique
    private void gtceu$renderContextAwareOutline(LevelRenderer instance, PoseStack poseStack, VertexConsumer consumer,
                                                 Entity entity, double camX, double camY, double camZ,
                                                 BlockPos pos, BlockState state, Operation<Void> original) {
        assert level != null;
        var rendererCfg = ConfigHolder.INSTANCE.client.renderer;
        int rgb = 0;
        boolean renderColoredOutline = false;

        // spotless:off
        MaterialEntry materialEntry = ChemicalHelper.getMaterialEntry(state.getBlock());
        if (rendererCfg.coloredMaterialBlockOutline && !materialEntry.isEmpty()) {
            renderColoredOutline = true;
            rgb = materialEntry.material().getMaterialRGB();
        } else if (rendererCfg.coloredTieredMachineOutline) {
                if (level.getBlockEntity(pos) instanceof SteamMachine steam) {
                    renderColoredOutline = true;
                    rgb = steam.isHighPressure() ? GTValues.VC_HP_STEAM : GTValues.VC_LP_STEAM;
                } else if (level.getBlockEntity(pos) instanceof ITieredMachine tiered) {
                    renderColoredOutline = true;
                    rgb = GTValues.VCM[tiered.getTier()];
                }
        } else if (rendererCfg.coloredWireOutline && level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe) {
            renderColoredOutline = true;
            if (!pipe.getFrameMaterial().isNull()) {
                rgb = pipe.getFrameMaterial().getMaterialRGB();
            } else if (pipe instanceof CableBlockEntity cable) {
                rgb = GTValues.VCM[GTUtil.getTierByVoltage(cable.getNodeData().getVoltage())];
            } else if (state.getBlock() instanceof MaterialPipeBlock<?,?,?> materialPipe) {
                rgb = materialPipe.material.getMaterialRGB();
            }
        }
        // spotless:on
        VoxelShape blockShape = state.getShape(level, pos, CollisionContext.of(entity));

        if (renderColoredOutline) {
            float red = FastColor.ARGB32.red(rgb) / 255f;
            float green = FastColor.ARGB32.green(rgb) / 255f;
            float blue = FastColor.ARGB32.blue(rgb) / 255f;
            renderShape(poseStack, consumer, blockShape,
                    pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                    red, green, blue, 0.4f);
            return;
        }
        original.call(instance, poseStack, consumer, entity, camX, camY, camZ, pos, state);
    }
}
