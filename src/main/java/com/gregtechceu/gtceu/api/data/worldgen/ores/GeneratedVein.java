package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Holds a vein's {@link OreBlockPlacer}s for each of its blocks, grouped by chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GeneratedVein {

    @Getter
    private final ChunkPos origin;

    @Getter
    private final IWorldGenLayer layer;

    private final Long2ObjectMap<OreBlockPlacer> oresBySection;

    /**
     * @param origin        The vein's origin chunk (NOT its actual center, which may be outside the origin chunk)
     * @param oresBySection The ore placers for each ore block position grouped by chunk.<br>
     *                      Doesn't need to be ordered, grouping by chunks is done internally.
     */
    public GeneratedVein(ChunkPos origin, IWorldGenLayer layer,
                         Long2ObjectMap<OreBlockPlacer> oresBySection) {
        this.origin = origin;
        this.layer = layer;
        this.oresBySection = oresBySection;
    }

    /**
     * Retrieve the ore placers for all blocks inside the specified chunk.
     */
    public OreBlockPlacer consumeOres(long sectionPos) {
        return this.oresBySection.get(sectionPos);
    }

    public LongSet getGeneratedSections() {
        return oresBySection.keySet();
    }

    @Override
    public String toString() {
        return "GeneratedVein[origin=" + origin + ", chunks={" +
                oresBySection.keySet().stream().map(SectionPos::of).map(SectionPos::toString).collect(Collectors.joining(", ")) + "}]";
    }
}
