package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemDecoratorComponent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

import com.mojang.blaze3d.systems.RenderSystem;

public final class GTComponentItemDecorator implements IItemDecorator {

    public static final GTComponentItemDecorator INSTANCE = new GTComponentItemDecorator();

    private GTComponentItemDecorator() {}

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof IComponentItem componentItem)) {
            return false;
        }
        for (var component : componentItem.getComponents()) {
            if (component instanceof IItemDecoratorComponent itemDecoratorComponent) {
                if (itemDecoratorComponent.render(guiGraphics, font, stack, xOffset, yOffset)) {
                    resetRenderState();
                }
            }
        }
        // we reset the state ourselves, so just return false
        return false;
    }

    private void resetRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
