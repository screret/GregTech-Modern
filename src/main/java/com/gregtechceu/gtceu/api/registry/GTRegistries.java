package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public final class GTRegistries {

    // spotless:off
    public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN_REGISTRY = GTRegistration.REGISTRATE.makeDatapackRegistry("ore_vein", GTOreDefinition.DIRECT_CODEC, GTOreDefinition.DIRECT_CODEC);
    public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID_REGISTRY = GTRegistration.REGISTRATE.makeDatapackRegistry("bedrock_fluid", BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.DIRECT_CODEC);
    public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE_REGISTRY = GTRegistration.REGISTRATE.makeDatapackRegistry("bedrock_ore", BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.DIRECT_CODEC);

    public static final ResourceKey<Registry<Element>> ELEMENT_REGISTRY = makeRegistryKey(GTCEu.id("element"));
    public static final ResourceKey<Registry<TagPrefix>> TAG_PREFIX_REGISTRY = makeRegistryKey(GTCEu.id("tag_prefix"));
    public static final ResourceKey<Registry<Material>> MATERIAL_REGISTRY = makeRegistryKey(GTCEu.id("material"));

    public static final ResourceKey<Registry<MachineDefinition>> MACHINE_REGISTRY = makeRegistryKey(GTCEu.id("machine"));
    public static final ResourceKey<Registry<CoverDefinition>> COVER_REGISTRY = makeRegistryKey(GTCEu.id("cover"));

    public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY_REGISTRY = makeRegistryKey(GTCEu.id("recipe_category"));
    public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY_REGISTRY = makeRegistryKey(GTCEu.id("recipe_capability"));
    public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION_REGISTRY = makeRegistryKey(GTCEu.id("recipe_condition"));
    public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC_REGISTRY = makeRegistryKey(GTCEu.id("chance_logic"));

    public static final ResourceKey<Registry<SoundEntry>> SOUND_REGISTRY = makeRegistryKey(GTCEu.id("sound"));
    public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER_REGISTRY = makeRegistryKey(GTCEu.id("dimension_marker"));

    public static final Supplier<IForgeRegistry<Element>> ELEMENTS = GTRegistration.REGISTRATE.createRegistry(ELEMENT_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<TagPrefix>> TAG_PREFIXES = GTRegistration.REGISTRATE.createRegistry(TAG_PREFIX_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<Material>> MATERIALS = GTRegistration.REGISTRATE.createRegistry(MATERIAL_REGISTRY, GTRegistries::makeMaterialRegistry);

    public static final Supplier<IForgeRegistry<SoundEntry>> SOUNDS = GTRegistration.REGISTRATE.createRegistry(SOUND_REGISTRY, () -> GTRegistries.makeRegistry(false));
    public static final Supplier<IForgeRegistry<ChanceLogic>> CHANCE_LOGICS = GTRegistration.REGISTRATE.createRegistry(CHANCE_LOGIC_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<RecipeCapability<?>>> RECIPE_CAPABILITIES = GTRegistration.REGISTRATE.createRegistry(RECIPE_CAPABILITY_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<RecipeConditionType<?>>> RECIPE_CONDITIONS = GTRegistration.REGISTRATE.createRegistry(RECIPE_CONDITION_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<GTRecipeCategory>> RECIPE_CATEGORIES = GTRegistration.REGISTRATE.createRegistry(RECIPE_CATEGORY_REGISTRY, GTRegistries::makeRegistry);

    public static final Supplier<IForgeRegistry<MachineDefinition>> MACHINES = GTRegistration.REGISTRATE.createRegistry(MACHINE_REGISTRY, GTRegistries::makeRegistry);
    public static final Supplier<IForgeRegistry<CoverDefinition>> COVERS = GTRegistration.REGISTRATE.createRegistry(COVER_REGISTRY, GTRegistries::makeRegistry);

    public static final Supplier<IForgeRegistry<DimensionMarker>> DIMENSION_MARKERS = GTRegistration.REGISTRATE.createRegistry(DIMENSION_MARKER_REGISTRY, () -> GTRegistries.makeRegistry(false));
    // spotless:on

    public static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    public static <T> RegistryBuilder<T> makeRegistry() {
        return makeRegistry(true);
    }

    public static <T> RegistryBuilder<T> makeRegistry(boolean sync) {
        RegistryBuilder<T> builder = RegistryBuilder.<T>of()
                .allowModification();
        if (!sync) {
            builder.disableSync();
        }
        return builder;
    }

    private static RegistryBuilder<Material> makeMaterialRegistry() {
        return RegistryBuilder.<Material>of()
                .onAdd(MaterialRegistryManager.getInstance()::onRegister)
                .onCreate(MaterialRegistryManager.getInstance()::onCreate)
                .onBake(MaterialRegistryManager.getInstance()::onFreeze);
    }

    private static final Table<Registry<?>, ResourceLocation, Object> TO_REGISTER = HashBasedTable.create();
    private static boolean isFrozen = true;

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (!isFrozen) {
            Registry.register(registry, name, value);
        } else {
            TO_REGISTER.put(registry, name, value);
        }
        return value;
    }

    // ignore the generics and hope the registered objects are still correctly typed :3
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void actuallyRegister(RegisterEvent event) {
        for (Registry reg : TO_REGISTER.rowKeySet()) {
            event.register(reg.key(), helper -> {
                TO_REGISTER.row(reg).forEach(helper::register);
            });
        }
        TO_REGISTER.clear();
    }

    public static void init() {}

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
