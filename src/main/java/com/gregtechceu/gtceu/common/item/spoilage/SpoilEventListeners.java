package com.gregtechceu.gtceu.common.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpoilEventListeners {

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        if (event.phase == TickEvent.Phase.START) {
            for (Entity entity : serverLevel.getAllEntities()) {
                // process spoilage updates for all loaded entities
                SpoilUtils.updateEntity(entity);
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            // the event is only given level chunks by forge, so something's gone funny
            return;
        }
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;

        BlockEntitySpoilTicker.get(level).chunkLoaded(chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;

        BlockEntitySpoilTicker.get(level).chunkUnloaded(chunk);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.level instanceof ServerLevel level)) return;

        BlockEntitySpoilTicker.get(level).tick();
    }
}
