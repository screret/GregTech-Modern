package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

public final class GTRegistries {

    public static final ResourceLocation ROOT_GT_REGISTRY_NAME = GTCEu.id("root");
    public static final GTRegistry<GTRegistry<?>> ROOT = new GTRegistry<>(ROOT_GT_REGISTRY_NAME);
    // spotless:off
    public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN_REGISTRY = makeRegistryKey(GTCEu.id("ore_vein"));
    public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID_REGISTRY = makeRegistryKey(GTCEu.id("bedrock_fluid"));
    public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE_REGISTRY = makeRegistryKey(GTCEu.id("bedrock_ore"));

    public static final ResourceKey<Registry<Material>> MATERIAL_REGISTRY = makeRegistryKey(GTCEu.id("material"));
    public static final ResourceKey<Registry<Element>> ELEMENT_REGISTRY = makeRegistryKey(GTCEu.id("element"));
    public static final ResourceKey<Registry<MachineDefinition>> MACHINE_REGISTRY = makeRegistryKey(GTCEu.id("machine"));
    public static final ResourceKey<Registry<CoverDefinition>> COVER_REGISTRY = makeRegistryKey(GTCEu.id("cover"));

    public static final ResourceKey<Registry<GTRecipeType>> RECIPE_TYPE_REGISTRY = makeRegistryKey(GTCEu.id("recipe_type"));
    public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY_REGISTRY = makeRegistryKey(GTCEu.id("recipe_category"));
    public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY_REGISTRY = makeRegistryKey(GTCEu.id("recipe_capability"));
    public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION_REGISTRY = makeRegistryKey(GTCEu.id("recipe_condition"));
    public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC_REGISTRY = makeRegistryKey(GTCEu.id("chance_logic"));

    public static final ResourceKey<Registry<SoundEntry>> SOUND_REGISTRY = makeRegistryKey(GTCEu.id("sound"));
    public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER_REGISTRY = makeRegistryKey(GTCEu.id("dimension_marker"));

    // GT Registry
    public static final GTRegistry<Material> MATERIALS = new GTRegistry<>(MATERIAL_REGISTRY);
    public static final GTRegistry<Element> ELEMENTS = new GTRegistry<>(ELEMENT_REGISTRY);
    public static final GTRegistry<MachineDefinition> MACHINES = new GTRegistry<>(MACHINE_REGISTRY);
    public static final GTRegistry<CoverDefinition> COVERS = new GTRegistry<>(COVER_REGISTRY);

    public static final GTRegistry<GTRecipeType> RECIPE_TYPES = new GTRegistry<>(RECIPE_TYPE_REGISTRY);
    public static final GTRegistry<GTRecipeCategory> RECIPE_CATEGORIES = new GTRegistry<>(RECIPE_CATEGORY_REGISTRY);
    public static final GTRegistry<RecipeCapability<?>> RECIPE_CAPABILITIES = new GTRegistry<>(RECIPE_CAPABILITY_REGISTRY);
    public static final GTRegistry<RecipeConditionType<?>> RECIPE_CONDITIONS = new GTRegistry<>(RECIPE_CONDITION_REGISTRY);
    public static final GTRegistry<ChanceLogic> CHANCE_LOGICS = new GTRegistry<>(CHANCE_LOGIC_REGISTRY);

    public static final GTRegistry<SoundEntry> SOUNDS = new GTRegistry<>(SOUND_REGISTRY);
    public static final GTRegistry<DimensionMarker> DIMENSION_MARKERS = new GTRegistry<>(DIMENSION_MARKER_REGISTRY);
    // spotless:on

    public static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = DeferredRegister
            .create(Registries.TRUNK_PLACER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER = DeferredRegister
            .create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIES = DeferredRegister
            .create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        ResourceKey<?> registryKey = registry.key();

        if (registryKey == Registries.RECIPE_TYPE) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registryKey == Registries.RECIPE_SERIALIZER) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else if (registryKey == Registries.FEATURE) {
            ForgeRegistries.FEATURES.register(name, (Feature<?>) value);
        } else if (registryKey == Registries.FOLIAGE_PLACER_TYPE) {
            ForgeRegistries.FOLIAGE_PLACER_TYPES.register(name, (FoliagePlacerType<?>) value);
        } else if (registryKey == Registries.TRUNK_PLACER_TYPE) {
            TRUNK_PLACER_TYPE.register(name.getPath(), () -> (TrunkPlacerType<?>) value);
        } else if (registryKey == Registries.PLACEMENT_MODIFIER_TYPE) {
            PLACEMENT_MODIFIER.register(name.getPath(), () -> (PlacementModifierType<?>) value);
        } else {
            return Registry.register(registry, name, value);
        }

        return value;
    }

    public static void init(IEventBus eventBus) {
        TRUNK_PLACER_TYPE.register(eventBus);
        PLACEMENT_MODIFIER.register(eventBus);
        GLOBAL_LOOT_MODIFIES.register(eventBus);
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (GTCEu.isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return FROZEN;
    }

    private static class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            } else {
                return FROZEN;
            }
        }
    }
}
