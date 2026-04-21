package com.gregtechceu.gtceu.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    // capture the return value of removeBlock
    @ModifyExpressionValue(method = "destroyBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean gtceu$captureBlockRemoveResult(boolean original,
                                                   @Share("removed") LocalBooleanRef removed) {
        removed.set(original);
        return original;
    }


    // ...and return that value instead of the original (which is always true)
    // this is technically more correct than vanilla's behavior and the return value is only used for debug logging

    // spotless:off  disable spotless here to keep the newlines intact
    @ModifyReturnValue(method = "destroyBlock", at = @At("RETURN"),
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z")
            )
    )
    // spotless:on
    private boolean gtceu$changeDestroyBlockReturnValue(boolean original,
                                                        @Share("removed") LocalBooleanRef removed) {
        return original && removed.get();
    }
}
