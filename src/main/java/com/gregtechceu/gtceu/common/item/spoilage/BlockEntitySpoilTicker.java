package com.gregtechceu.gtceu.common.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.LevelAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.ticks.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySpoilTicker extends SavedData {

    private static final LevelAttached<BlockEntitySpoilTicker> TICKERS = new LevelAttached<>(BlockEntitySpoilTicker::new, BlockEntitySpoilTicker::delete);

    public static BlockEntitySpoilTicker get(ServerLevel level) {
        return TICKERS.get(level);
    }

    private final ServerLevel level;
    @Getter
    private final LevelTicks<BlockEntityType<?>> blockEntityTicks;

    private BlockEntitySpoilTicker(LevelAccessor levelAccessor) {
        // noinspection PatternVariableHidesField shush
        if (!(levelAccessor instanceof ServerLevel level)) {
            throw new IllegalArgumentException("SpoilageContainerTracker only works on servers!");
        }

        this.level = level;
        this.blockEntityTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, level.getProfilerSupplier());

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void delete() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private <T> ScheduledTick<T> createTick(BlockPos pos, T type, long delay, TickPriority priority) {
        return new ScheduledTick<>(type, pos, level.getLevelData().getGameTime() + delay, priority,
                level.nextSubTickCount());
    }

    public void scheduleTick(BlockPos pos, BlockEntityType<?> type, long delay, TickPriority priority) {
        this.blockEntityTicks.schedule(this.createTick(pos, type, delay, priority));
    }

    public void scheduleTick(BlockEntity blockEntity, long delay, TickPriority priority) {
        scheduleTick(blockEntity.getBlockPos(), blockEntity.getType(), delay, priority);
    }

    // region event listeners

    public void tick() {
        long gameTime = this.level.getGameTime();
        this.blockEntityTicks.tick(gameTime, ServerLevel.MAX_SCHEDULED_TICKS_PER_TICK, (pos, blockEntityType) -> {
            BlockEntity blockEntity = this.level.getBlockEntity(pos);
            if (blockEntity == null || blockEntity.isRemoved() || blockEntity.getType() != blockEntityType) {
                // if the block entity no longer exists, exit without doing anything (including rescheduling)
                return;
            }

            SpoilUtils.updateBlock(blockEntity);

            // reschedule another update for the next tick
            this.scheduleTick(pos, blockEntityType, 1L, TickPriority.NORMAL);
        });
    }

    public void chunkLoaded(LevelChunk chunk) {
        // noinspection DataFlowIssue shush
        ChunkBlockEntityTickHolder holder = chunk.getCapability(ChunkBlockEntityTickHolder.CAPABILITY).orElse(null);
        // noinspection ConstantValue shush
        if (holder == null) return; // "How did we get here?"

        this.blockEntityTicks.addContainer(chunk.getPos(), holder.blockEntityTicks);

        chunk.getBlockEntities().values().forEach((blockEntity) -> {
            // only process block entities with item handlers
            if (!hasItemHandler(blockEntity)) return; // nvm it can change whenever

            this.scheduleTick(blockEntity, 0L, TickPriority.EXTREMELY_HIGH);
        });
    }

    public void chunkUnloaded(LevelChunk chunk) {
        this.blockEntityTicks.removeContainer(chunk.getPos());
    }

    @ApiStatus.Internal
    public static void registerEventListeners(IEventBus modBus) {
        // modBus.addListener(ChunkBlockEntityTickHolder::registerCapability);
        // MinecraftForge.EVENT_BUS.register(ChunkBlockEntityTickHolder.class);
    }

    private boolean isPositionTickingWithEntitiesLoaded(long chunkPos) {
        return level.areEntitiesLoaded(chunkPos) && level.getChunkSource().isPositionTicking(chunkPos);
    }

    private boolean hasItemHandler(BlockEntity blockEntity) {
        LazyOptional<IItemHandler> value;
        for (Direction side : GTUtil.DIRECTIONS) {
            value = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            if (value.isPresent()) {
                return true;
            }
        }
        value = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        // noinspection RedundantIfStatement more readable this way
        if (value.isPresent()) {
            return true;
        }

        return false;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return compoundTag;
    }

    @ApiStatus.Internal
    public static class ChunkBlockEntityTickHolder implements ICapabilitySerializable<ListTag> {

        @ApiStatus.Internal
        public static final Capability<ChunkBlockEntityTickHolder> CAPABILITY = CapabilityManager
                .get(new CapabilityToken<>() {});

        private final LevelChunkTicks<BlockEntityType<?>> blockEntityTicks;
        private final LevelChunk chunk;

        private final LazyOptional<ChunkBlockEntityTickHolder> lazy = LazyOptional.of(() -> this);

        private ChunkBlockEntityTickHolder(LevelChunk chunk) {
            this.blockEntityTicks = new LevelChunkTicks<>();
            this.chunk = chunk;
        }

        @Override
        public ListTag serializeNBT() {
            long gameTime = this.chunk.getLevel().getGameTime();
            return this.blockEntityTicks.save(gameTime, (type) -> {
                // noinspection deprecation,DataFlowIssue
                return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type).toString();
            });
        }

        @Override
        public void deserializeNBT(ListTag tag) {
            ChunkPos pos = this.chunk.getPos();
            final long gameTime = this.chunk.getLevel().getGameTime();

            // clear old data if any exists
            this.blockEntityTicks.removeIf(scheduledTick -> true);
            // load fresh data in its place
            final MutableInt subtickOrder = new MutableInt(-tag.size());
            SavedTick.loadTickList(tag, (id) -> {
                // noinspection deprecation
                return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(ResourceLocation.tryParse(id));
            }, pos, savedTick -> {
                this.blockEntityTicks.schedule(savedTick.unpack(gameTime, subtickOrder.getAndIncrement()));
            });
        }

        public void startTickingChunk(long gameTime) {
            this.blockEntityTicks.unpack(gameTime);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, this.lazy);
        }

        @SubscribeEvent
        private static void attachChunkCapability(AttachCapabilitiesEvent<LevelChunk> event) {
            LevelChunk chunk = event.getObject();
            if (!chunk.getLevel().isClientSide) return;

            event.addCapability(GTCEu.id("block_entity_ticks"), new ChunkBlockEntityTickHolder(chunk));
        }

        private static void registerCapability(RegisterCapabilitiesEvent event) {
            event.register(ChunkBlockEntityTickHolder.class);
        }
    }
}
