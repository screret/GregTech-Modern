package com.gregtechceu.gtceu.api.registry;

import com.google.common.collect.Maps;
import com.gregtechceu.gtceu.GTCEu;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class GTRegistry<T> extends MappedRegistry<T> {

    public static final Map<ResourceKey<?>, GTRegistry<?>> REGISTERED = new HashMap<>();

    public GTRegistry(ResourceLocation registryName) {
        this(ResourceKeyAccessor.callCreate(GTRegistries.ROOT_GT_REGISTRY_NAME, registryName));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GTRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        super(registryKey, Lifecycle.stable());
        GTRegistries.ROOT.register((ResourceKey) registryKey, this, RegistrationInfo.BUILT_IN);
    }

    @Override
    public Registry<T> freeze() {
        if (!checkActiveModContainerIsOwner()) {
            return this;
        }
        return super.freeze();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void unfreeze() {
        if (!checkActiveModContainerIsOwner()) {
            return;
        }
        super.unfreeze();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkActiveModContainerIsOwner() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        return container != null && (container.getModId().equals(this.key().location().getNamespace()) ||
                container.getModId().equals(GTCEu.MOD_ID) ||
                container.getModId().equals("minecraft")); // check for minecraft in case of datagen or a mishap
    }

    public <V extends T> V register(ResourceKey<T> key, V value) {
        if (containsKey(key)) {
            throw new IllegalStateException(
                    "[GTRegistry] registry %s contains key %s already".formatted(key().location(), key.location()));
        }
        return this.registerOrOverride(key, value);
    }

    public <V extends T> V register(ResourceLocation key, V value) {
        return this.register(ResourceKey.create(this.key(), key), value);
    }

    @Nullable
    public <V extends T> V replace(ResourceLocation key, V value) {
        return this.replace(ResourceKey.create(this.key(), key), value);
    }

    @Nullable
    public <V extends T> V replace(ResourceKey<T> key, V value) {
        if (!containsKey(key)) {
            GTCEu.LOGGER.warn("[GTRegistry] couldn't find key {} in registry {}", key, key().location());
        }
        registerOrOverride(key, value);
        return value;
    }

    public <V extends T> V registerOrOverride(ResourceLocation key, V value) {
        return this.registerOrOverride(ResourceKey.create(this.key(), key), value);
    }

    public <V extends T> V registerOrOverride(ResourceKey<T> key, V value) {
        super.register(key, value, RegistrationInfo.BUILT_IN);
        return value;
    }

    @UnmodifiableView
    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return super.entrySet();
    }

    @UnmodifiableView
    public Set<Map.Entry<ResourceLocation, T>> entries() {
        return Collections.unmodifiableSet(Maps.transformValues(this.getByLocation(), Holder::value).entrySet());
    }

    @UnmodifiableView
    public Map<ResourceLocation, T> registry() {
        return Collections.unmodifiableMap(Maps.transformValues(this.getByLocation(), Holder::value));
    }

    public T getOrDefault(ResourceLocation name, T defaultValue) {
        T value = get(name);
        return value != null ? value : defaultValue;
    }

    public ResourceLocation getOrDefaultKey(T value, ResourceLocation defaultKey) {
        ResourceLocation key = getKey(value);
        return key != null ? key : defaultKey;
    }

    public boolean remove(ResourceLocation key) {
        return remove(ResourceKey.create(this.key(), key));
    }

    public boolean remove(ResourceKey<T> key) {
        Holder<T> holder = this.getHolder(key).orElse(null);
        if (holder == null) {
            return false;
        }
        return remove(holder);
    }

    public boolean remove(Holder<T> holder) {
        ResourceKey<T> key = holder.getKey();
        boolean removed = true;
        removed &= this.getByKey().remove(key) != null;
        removed &= this.getByLocation().remove(key.location()) != null;
        removed &= this.getByValue().remove(holder.value()) != null;
        removed &= this.getById().remove(holder);
        removed &= this.getToId().removeInt(holder.value()) != -1;
        removed &= this.getRegistrationInfos().remove(key) != null;
        removed &= this.getRegistrationInfos().remove(key) != null;

        return removed;
    }

    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return new StreamCodec<>() {
            public T decode(RegistryFriendlyByteBuf buffer) {
                return GTRegistry.this.byIdOrThrow(VarInt.read(buffer));
            }

            public void encode(RegistryFriendlyByteBuf buffer, T value) {
                VarInt.write(buffer, GTRegistry.this.getIdOrThrow(value));
            }
        };
    }

    @Override
    public void clear(boolean full) {
        super.clear(full);
    }
}
