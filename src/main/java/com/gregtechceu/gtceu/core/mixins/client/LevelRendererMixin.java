package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.core.IBlockDestructionProgressExt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
        if (minecraft.player.isShiftKeyDown() || mainHandItem.isEmpty() || !ToolHelper.hasBehaviorsTag(mainHandItem) ||
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

    @WrapOperation(method = "renderHitOutline",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"))
    private void gtceu$renderContextAwareOutline(PoseStack poseStack, VertexConsumer consumer,
                                                 VoxelShape shape, double x, double y, double z,
                                                 float red, float green, float blue, float alpha,
                                                 Operation<Void> original,
                                                 @Local(argsOnly = true) BlockPos pos,
                                                 @Local(argsOnly = true) BlockState state) {
        int rgb = RenderUtil.getBlockOutlineColor(level, pos, state);

        // only override color if we changed it so other mods' patches don't get ignored
        if (rgb != 0) {
            red = FastColor.ARGB32.red(rgb) / 255f;
            green = FastColor.ARGB32.green(rgb) / 255f;
            blue = FastColor.ARGB32.blue(rgb) / 255f;
        }

        original.call(poseStack, consumer, shape, x, y, z, red, green, blue, alpha);
    }
}
