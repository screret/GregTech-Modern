package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.item.SpoilableItemStack;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class SpoilUtils {

    public static void mergeSpoilage(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() || other.isEmpty() || !stack.hasTag() || !other.hasTag() ||
                stack.getOrCreateTag().equals(other.getOrCreateTag())) {
            return;
        }
        ISpoilableItem first = GTCapabilityHelper.getSpoilable(stack);
        ISpoilableItem second = GTCapabilityHelper.getSpoilable(other);
        if (first instanceof SpoilableItemStack spoilable1 && second instanceof SpoilableItemStack spoilable2) {
            spoilable1.prepareForComparisonWith(spoilable2);
        }
    }

    /**
     * Initializes this ItemStack's spoilage timer if it wasn't initialized before.
     * Should be called when it finishes crafting, for example.
     */
    public static void update(ItemStack stack, SpoilContext spoilContext) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable != null) spoilable.updateFreshness(spoilContext, true);
    }

    public static void updateBlock(Level level, BlockPos pos) {
        updateBlock(level.getBlockEntity(pos));
    }

    public static void updateBlock(BlockEntity blockEntity) {
        for (Direction side : GTUtil.DIRECTIONS) updateBlock(blockEntity, side);
        updateBlock(blockEntity, null);
    }

    public static void updateBlock(BlockEntity blockEntity, Direction context) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, context)
                .ifPresent(handler -> updateBlockItemHandler(handler, blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    public static void updateBlockItemHandler(IItemHandler handler, Level level, @Nullable BlockPos pos) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(level, pos, handler, slot), true);
            }
        }
    }

    public static void updateEntity(Entity entity) {
        // technically this updates player's armor/offhand slots 3 times, but I don't think that matters too much.
        for (Direction side : GTUtil.DIRECTIONS) updateEntity(entity, side);
        updateEntity(entity, null);
    }

    public static void updateEntity(Entity entity, Direction context) {
        entity.getCapability(ForgeCapabilities.ITEM_HANDLER, context)
                .ifPresent(handler -> updateEntityItemHandler(handler, entity));
    }

    public static void updateEntityItemHandler(IItemHandler handler, Entity entity) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(entity, handler, slot), true);
            }
        }
    }

    public static void spawnEntity(SpoilContext spoilContext, EntityType<? extends Mob> type, int count) {
        if (type == null) return;
        if (!(spoilContext.level() instanceof ServerLevel level)) return;

        BlockPos pos;
        if (spoilContext.entity() != null) pos = spoilContext.entity().blockPosition();
        else if (spoilContext.pos() != null) pos = spoilContext.pos();
        else return;

        if (level.getBlockState(pos).isSuffocating(level, pos)) {
            for (Direction side : GTUtil.DIRECTIONS) {
                BlockPos relative = pos.relative(side);
                if (!level.getBlockState(relative).isSuffocating(level, relative)) {
                    pos = relative;
                    break;
                }
            }
        }
        for (int i = 0; i < count; i++) {
            type.spawn(level, pos, MobSpawnType.CONVERSION);
        }
    }
}
