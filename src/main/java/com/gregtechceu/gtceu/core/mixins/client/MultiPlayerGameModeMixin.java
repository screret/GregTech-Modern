package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @WrapOperation(method = "destroyBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;onDestroyedByPlayer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/level/material/FluidState;)Z"))
    private boolean gtceu$destroyBlock(BlockState state, Level level, BlockPos pos,
                                       Player player, boolean willHarvest, FluidState fluidState,
                                       Operation<Boolean> original) {
        if (player.isShiftKeyDown()) {
            return original.call(state, level, pos, player, willHarvest, fluidState);
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (!ToolHelper.hasBehaviorsTag(mainHandItem) ||
                ToolHelper.getAoEDefinition(mainHandItem).isZero() ||
                !mainHandItem.isCorrectToolForDrops(state)) {
            return original.call(state, level, pos, player, willHarvest, fluidState);
        }
        return true;
    }

    @WrapOperation(method = "sameDestroyTarget",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$sameDestroyTarget(ItemStack mainHandItem, ItemStack destroyingItem,
                                            Operation<Boolean> original) {
        // Fix Tool charging resetting block break progress
        if (mainHandItem.getItem() instanceof IGTTool && destroyingItem.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(mainHandItem, destroyingItem);
        } else {
            return original.call(mainHandItem, destroyingItem);
        }
    }
}
