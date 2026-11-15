package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.item.spoilage.SpoilUtils;
import com.gregtechceu.gtceu.common.item.spoilage.SpoilableItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/**
 * This object holds all spoilage-related data for specific item types,
 * NOT individual item stacks. This class handles attaching the {@link ISpoilableItem}
 * capability by registering its' instances to the {@link AttachCapabilitiesEvent}
 */
public class SpoilableBehavior {

    private final ToLongFunction<ItemStack> ticks;
    private final SpoilResultProvider spoilResult;
    private final Function<ItemStack, Component> spoilsIntoTooltip;
    private final Set<Item> attachedTo = new ReferenceOpenHashSet<>();

    private SpoilableBehavior(ToLongFunction<ItemStack> ticks, SpoilResultProvider spoilResult,
                              Function<ItemStack, Component> spoilsIntoTooltip) {
        this.ticks = ticks;
        this.spoilResult = spoilResult;
        this.spoilsIntoTooltip = spoilsIntoTooltip;
    }

    /**
     * Registers this object to the {@link AttachCapabilitiesEvent} if it wasn't registered yet,
     * and adds the specified item to the list of items that this will attach to.
     * 
     * @param item the item to attach the behavior to
     * @return this
     */
    public SpoilableBehavior attachTo(ItemLike item) {
        if (attachedTo.isEmpty()) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        attachedTo.add(item.asItem());
        return this;
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (attachedTo.contains(stack.getItem())) {
            event.addCapability(GTCEu.id("spoilable"), new SpoilableBehaviourStack(stack));
        }
    }

    @ApiStatus.Internal
    public static Builder builder() {
        return new Builder();
    }

    // they are initialized. just with builder functions instead of direct field assignment.
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static class Builder {

        private ToLongFunction<ItemStack> ticks;
        private SpoilResultProvider result;
        private Function<ItemStack, Component> tooltip;

        private Builder() {
            this.ticks(100);
            this.result(ItemStack.EMPTY); // sets both result and tooltip
        }

        public SpoilableBehavior build() {
            Objects.requireNonNull(ticks, "tick provider not set");
            Objects.requireNonNull(result, "result provider not set");
            Objects.requireNonNull(tooltip, "tooltip provider not set");

            return new SpoilableBehavior(ticks, result, tooltip);
        }

        public Builder ticks(ToLongFunction<ItemStack> ticks) {
            this.ticks = ticks;
            return this;
        }

        public Builder ticks(long ticks) {
            return ticks(stack -> ticks);
        }

        public Builder result(SpoilResultProvider result) {
            this.result = result;
            return this;
        }

        public Builder result(ItemLike itemLike) {
            final Item item = itemLike.asItem();
            return result(oldStack -> item.getDefaultInstance().copyWithCount(oldStack.getCount()));
        }

        public Builder result(ItemStack stack) {
            return result(oldStack -> stack.copyWithCount(oldStack.getCount()));
        }

        public Builder result(Function<ItemStack, ItemStack> result) {
            return result((stack, spoilContext, simulate) -> result.apply(stack))
                    .tooltip(stack -> {
                        ItemStack resultStack = result.apply(stack);
                        if (resultStack.isEmpty()) return Component.empty();
                        return resultStack.getHoverName();
                    });
        }

        public Builder result(EntityType<? extends Mob> entityType) {
            return result(() -> entityType);
        }

        public Builder result(Supplier<? extends EntityType<? extends Mob>> entityType) {
            final SpoilResultProvider previousResult = result;
            final Function<ItemStack, Component> previousTooltip = tooltip;

            return result((stack, spoilContext, simulate) -> {
                if (!simulate) {
                    EntityType<? extends Mob> type = entityType.get();
                    SpoilUtils.spawnEntity(spoilContext, type, stack.getCount());
                }
                return previousResult.getSpoilResult(stack, spoilContext, simulate);
            }).tooltip(stack -> {
                EntityType<? extends Mob> type = entityType.get();
                MutableComponent component = type.getDescription().copy();
                Component previous = previousTooltip.apply(stack);
                if (!previous.getString().isEmpty()) component.append(", ").append(previous);
                return component;
            });
        }

        public Builder multiplyResult(int multiplier) {
            final SpoilResultProvider previousResult = result;
            final Function<ItemStack, Component> previousTooltip = tooltip;

            return result((stack, spoilContext, simulate) -> {
                ItemStack total = previousResult.getSpoilResult(stack, spoilContext, simulate);
                for (int i = 1; i < multiplier; i++) {
                    ItemStack temp = previousResult.getSpoilResult(stack, spoilContext, simulate);
                    if (ItemStack.isSameItemSameTags(total, temp)) {
                        total.grow(temp.getCount());
                    }
                }
                return total;
            }).tooltip(stack -> {
                return Component.literal("(")
                        .append(previousTooltip.apply(stack))
                        .append(") x").append(Integer.toString(multiplier));
            });
        }

        public Builder tooltip(Function<ItemStack, Component> tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder tooltip(Component tooltip) {
            return tooltip(stack -> tooltip);
        }
    }

    /**
     * An implementation of {@link ISpoilableItem} which returns spoil information from its'
     * {@code SpoilableBehavior} object.
     */
    public class SpoilableBehaviourStack extends SpoilableItemStack {

        private SpoilableBehaviourStack(ItemStack stack) {
            super(stack);
        }

        @Override
        public long getSpoilTicks() {
            return SpoilableBehavior.this.ticks.applyAsLong(getStack());
        }

        @Override
        public ItemStack spoilResult(SpoilContext spoilContext, boolean simulate) {
            return SpoilableBehavior.this.spoilResult.getSpoilResult(getStack(), spoilContext, simulate);
        }

        @Override
        protected Component getSpoilResultTooltip() {
            return SpoilableBehavior.this.spoilsIntoTooltip.apply(getStack());
        }
    }

    @FunctionalInterface
    public interface SpoilResultProvider {

        ItemStack getSpoilResult(ItemStack stack, SpoilContext spoilContext, boolean simulate);
    }
}
