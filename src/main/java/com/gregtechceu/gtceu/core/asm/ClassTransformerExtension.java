package com.gregtechceu.gtceu.core.asm;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.*;
import java.util.function.Consumer;

public final class ClassTransformerExtension implements IExtension {

    @ApiStatus.Internal
    public static final ClassTransformerExtension INSTANCE = new ClassTransformerExtension();

    private static final Map<String, List<Consumer<ITargetClassContext>>> TRANSFORMERS = new HashMap<>();

    public static void enqueueTransformer(String className, Consumer<ITargetClassContext> task) {
        TRANSFORMERS.computeIfAbsent(className.replace('.', '/'), k -> new ArrayList<>()).add(task);
    }

    public static void enqueueTransformer(Type classDescriptor, Consumer<ITargetClassContext> task) {
        TRANSFORMERS.computeIfAbsent(classDescriptor.getInternalName(), k -> new ArrayList<>()).add(task);
    }

    public static void enqueueTransformer(IClassTransformer transformer) {
        enqueueTransformer(transformer.getTargetTypeDescriptor(), transformer);
    }

    private ClassTransformerExtension() {}


    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {}

    @Override
    public void postApply(ITargetClassContext context) {
        String targetName = context.getClassNode().name;
        List<Consumer<ITargetClassContext>> tasks = TRANSFORMERS.remove(targetName);
        if (tasks != null) {
            for (var task : tasks) {
                task.accept(context);
            }
        }
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {}
}
