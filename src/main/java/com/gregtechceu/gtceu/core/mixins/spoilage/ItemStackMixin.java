package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.*;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
