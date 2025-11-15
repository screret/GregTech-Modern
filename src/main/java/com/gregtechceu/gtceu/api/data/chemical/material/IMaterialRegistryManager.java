package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.stream.Stream;

public interface IMaterialRegistryManager extends Iterable<Material> {

    /**
     * @return all namespaces the registered materials use
     */
    @NotNull
    @UnmodifiableView
    Collection<String> getUsedNamespaces();

    /**
     * Register a material.<br>
     * You must register any materials in a registration event
     * with {@link net.minecraftforge.eventbus.api.EventPriority#HIGHEST priority=HIGHEST},
     * as anything else may be skipped if your mod is loaded after GT.
     *
     * @param material the material to register
     * @return the same material.
     */
    Material register(Material material);

    /**
     * Get a material from a String in formats:
     * <ul>
     * <li>{@code "modid:registry_name"}</li>
     * <li>{@code "registry_name"} - where modid is inferred to be {@link GTCEu#MOD_ID}</li>
     * </ul>
     * Generally, you should use {@linkplain IMaterialRegistryManager#getMaterial(ResourceLocation)} instead.
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     * @see IMaterialRegistryManager#getMaterial(ResourceLocation)
     */
    default Material getMaterial(String name) {
        return getMaterial(GTCEu.id(name));
    }

    /**
     * Get a material from a ResourceLocation<br>
     * Intended for use in reading/writing materials from/to NBT tags.
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     * @see IMaterialRegistryManager#getMaterial(String)
     */
    Material getMaterial(ResourceLocation name);

    ResourceLocation getKey(Material material);

    /**
     * Set the fallback material for a namespace.
     * This is only for manual fallback usage.
     *
     * @param namespace the namespace to set the fallback for
     * @param material  the fallback material
     */
    void setFallbackMaterial(@NotNull String namespace, @NotNull Material material);

    /**
     * This is only for manual fallback usage.
     *
     * @param namespace the namespace to get the fallback for
     * @return the fallback material, used for when another material does not exist
     */
    @NotNull
    Material getFallbackMaterial(@NotNull String namespace);

    Stream<Material> stream();

    /**
     * @return the current phase in the material registration process
     * @see Phase
     */
    @NotNull
    Phase getPhase();

    default boolean canModifyMaterials() {
        return this.getPhase() != Phase.FROZEN;
    }

    default Codec<Material> codec() {
        return GTRegistries.MATERIALS.get().getCodec();
    }

    enum Phase {
        /** Material Registration and Modification is not started */
        PRE,
        /** Material Registration and Modification is available */
        OPEN,
        /** Material Registration is unavailable and only Modification is available */
        CLOSED,
        /** Material Registration and Modification is unavailable */
        FROZEN
    }
}
