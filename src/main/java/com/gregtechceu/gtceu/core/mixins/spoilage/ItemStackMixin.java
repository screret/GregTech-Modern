package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Unique
    private ItemStack gtceu$self() {
        return (ItemStack) (Object) this;
    }

    // ********* //
    // Injectors //
    // ********* //

    @Inject(method = "isSameItemSameTags",
            at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    private static void gtceu$mergeSpoilageDataBeforeComparison(ItemStack stack, ItemStack other, CallbackInfoReturnable<Boolean> cir) {
        SpoilUtils.mergeSpoilage(stack, other);
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IAddInformation} to
     * actually display the added tooltip.
     */
    @Inject(method = "getTooltipLines",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void gtceu$spoilageTooltip(Player player, TooltipFlag isAdvanced,
                                       CallbackInfoReturnable<List<Component>> cir,
                                       List<Component> list) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IAddInformation addInformation) {
            addInformation.appendHoverText(gtceu$self(), player == null ? null : player.level(), list,
                    isAdvanced);
        }
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void gtceu$spoilageBarVisible(CallbackInfoReturnable<Boolean> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.isBarVisible(gtceu$self()));
        }
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void gtceu$spoilageBarColor(CallbackInfoReturnable<Integer> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.getBarColor(gtceu$self()));
        }
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void gtceu$spoilageBarWidth(CallbackInfoReturnable<Integer> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.getBarWidth(gtceu$self()));
        }
    }
}
