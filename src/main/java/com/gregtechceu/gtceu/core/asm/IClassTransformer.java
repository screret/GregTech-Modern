package com.gregtechceu.gtceu.core.asm;

import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.function.Consumer;

/**
 * A custom class transformer interface that
 */
public interface IClassTransformer extends Consumer<ITargetClassContext> {

    @Override
    void accept(ITargetClassContext context);

    /**
     * The target class's type descriptor as a {@link Type}
     * @return The target class's type descriptor
     * @see Type#getDescriptor()
     */
    Type getTargetTypeDescriptor();
}
