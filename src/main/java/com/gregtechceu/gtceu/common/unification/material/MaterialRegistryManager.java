package com.gregtechceu.gtceu.common.unification.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MaterialRegistryManager implements IMaterialRegistryManager {

    private static MaterialRegistryManager INSTANCE;

    private final Set<String> usedNamespaces = new HashSet<>();
    private final Map<String, Material> fallbackMaterials = new HashMap<>();

    @Getter
    private Phase phase = Phase.PRE;

    private MaterialRegistryManager() {}

    public static MaterialRegistryManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialRegistryManager();
        }
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public void onRegister(IForgeRegistryInternal<Material> registry, RegistryManager stage, int id,
                           ResourceKey<Material> key, @NotNull Material value, @Nullable Material oldValue) {
        if (getPhase() == Phase.CLOSED || getPhase() == Phase.FROZEN) {
            throw new IllegalStateException(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the RegisterEvent. Skipping material %s..."
                            .formatted(key.location()));
        }
        usedNamespaces.add(key.location().getNamespace());
    }

    @SuppressWarnings("unused")
    public void onCreate(IForgeRegistryInternal<Material> registry, RegistryManager stage) {
        this.phase = Phase.OPEN;
    }

    @SuppressWarnings("unused")
    public void onFreeze(IForgeRegistryInternal<Material> registry, RegistryManager stage) {
        this.phase = Phase.FROZEN;
    }

    @Override
    public @UnmodifiableView @NotNull Collection<String> getUsedNamespaces() {
        return Collections.unmodifiableSet(usedNamespaces);
    }

    @Override
    public Material register(Material material) {
        GTRegistries.MATERIALS.get().register(material.getResourceLocation(), material);
        return material;
    }

    @Override
    public Material getMaterial(ResourceLocation name) {
        return GTRegistries.MATERIALS.get().getValue(name);
    }

    @Override
    public ResourceLocation getKey(Material material) {
        return GTRegistries.MATERIALS.get().getKey(material);
    }

    @Override
    public Stream<Material> stream() {
        return StreamSupport.stream(GTRegistries.MATERIALS.get().spliterator(), false);
    }

    @Override
    public @NotNull Iterator<Material> iterator() {
        return GTRegistries.MATERIALS.get().iterator();
    }

    /**
     * Set the fallback material for a namespace.
     * This is only for manual fallback usage.
     *
     * @param namespace the namespace to set the fallback for
     * @param material  the fallback material
     */
    @Override
    public void setFallbackMaterial(@NotNull String namespace, @NotNull Material material) {
        fallbackMaterials.put(namespace, material);
    }

    /**
     * This is only for manual fallback usage.
     *
     * @param namespace the namespace to get the fallback for
     * @return the fallback material, used for when another material does not exist
     */
    @Override
    @NotNull
    public Material getFallbackMaterial(@NotNull String namespace) {
        return fallbackMaterials.getOrDefault(namespace, getDefaultFallback());
    }

    @NotNull
    public Material getDefaultFallback() {
        return fallbackMaterials.get(GTCEu.MOD_ID);
    }

    public void unfreezeRegistries() {
        phase = Phase.OPEN;
    }

    public void closeRegistries() {
        phase = Phase.CLOSED;
    }
}
