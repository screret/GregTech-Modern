package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>,
        A extends HumanoidModel<T>> extends RenderLayer<T, M> {

    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Definition(id = "renderModel",
            method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
            remap = false)
    @Expression("this.renderModel(?, ?, ?, ?, ?, ?, 1.0, 1.0, 1.0, ?)")
    @ModifyArgs(method = "renderArmorPiece", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private void gtceu$modifyArmorTint(Args args, @Local ArmorItem armorItem) {
        if (armorItem instanceof GTArmorItem gtArmorItem) {
            int rgb = gtArmorItem.material.getMaterialRGB();

            args.set(6, FastColor.ARGB32.red(rgb) / 255.0f);
            args.set(7, FastColor.ARGB32.green(rgb) / 255.0f);
            args.set(8, FastColor.ARGB32.blue(rgb) / 255.0f);
        }
    }
}
