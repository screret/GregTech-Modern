package com.gregtechceu.gtceu.core.asm.impl;

import com.gregtechceu.gtceu.core.asm.IClassTransformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.util.Constants;

/**
 * Patch {@link net.minecraftforge.registries.ForgeRegistry}'s constructor to be public.
 */
public final class ForgeRegistryAccessTransformer implements IClassTransformer {

    // spotless:off
    private static final Type TARGET_CLASS = Type.getObjectType("net/minecraftforge/registries/ForgeRegistry");

    private static final Type REGISTRY_MANAGER = Type.getObjectType("net/minecraftforge/registries/RegistryManager");
    private static final Type RESOURCE_LOCATION = Type.getObjectType("net/minecraft/resources/ResourceLocation");
    private static final Type REGISTRY_BUILDER = Type.getObjectType("net/minecraftforge/registries/RegistryBuilder");

    private static final String TARGET_METHOD_NAME = Constants.CTOR;
    private static final String TARGET_METHOD_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, REGISTRY_MANAGER, RESOURCE_LOCATION, REGISTRY_BUILDER);
    private static final int TARGET_METHOD_ACC = 0;
    // spotless:on

    @Override
    public void accept(ITargetClassContext context) {
        ClassNode classNode = context.getClassNode();
        for (var method : classNode.methods) {
            if (!TARGET_METHOD_NAME.equals(method.name) || !TARGET_METHOD_DESC.equals(method.desc) || method.access != TARGET_METHOD_ACC) {
                continue;
            }
            method.access |= Opcodes.ACC_PUBLIC;
        }
    }

    @Override
    public Type getTargetTypeDescriptor() {
        return TARGET_CLASS;
    }
}
