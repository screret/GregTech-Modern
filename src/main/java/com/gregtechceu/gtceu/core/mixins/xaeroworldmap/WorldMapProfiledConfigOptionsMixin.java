package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosMapPlugin;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.lib.common.config.option.BooleanConfigOption;
import xaero.lib.common.config.option.ConfigOptionManager;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;

@Mixin(value = WorldMapProfiledConfigOptions.class, remap = false)
public class WorldMapProfiledConfigOptionsMixin {

    @Inject(method = "registerAll", at = @At("TAIL"))
    private static void gtceu$registerXaeroConfigOptions(ConfigOptionManager manager, CallbackInfo ci) {
        for (String layerName : Layers.allKeys()) {
            BooleanConfigOption option = BooleanConfigOption.Builder.begin()
                    .setId(layerName)
                    .setDefaultValue(false)
                    .setDisplayName(Component.translatable("gtceu.button." + layerName))
                    .build(null);

            XaerosMapPlugin.XAERO_OPTIONS.put(layerName, option);
            manager.register(option);
        }
    }
}
