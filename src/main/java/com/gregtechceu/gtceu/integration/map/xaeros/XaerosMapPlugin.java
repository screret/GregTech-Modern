package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.common.minimap.write.MinimapWriter;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.profile.ConfigProfile;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;

public class XaerosMapPlugin {

    public static boolean isActive = false;

    public static final Object2ObjectMap<String, ConfigOption<Boolean>> XAERO_OPTIONS = new Object2ObjectOpenHashMap<>();

    public static void init() {
        isActive = true;
    }

    public static void toggleOption(String name, @Nullable Boolean active) {
        trySettingCurrentProfileOption(XAERO_OPTIONS.get(name), active);

        MinimapWriter write = BuiltInHudModules.MINIMAP.getCurrentSession().getProcessor().getMinimapWriter();
        DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
        if (dimHighlightHandler != null) {
            dimHighlightHandler.requestRefresh();
        }

        if (ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration &&
                GTCEu.isModLoaded(GTValues.MODID_XAEROS_WORLDMAP)) {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            MapWorld world = session.getMapProcessor().getMapWorld();
            for (MapDimension mapDim : world.getDimensionsList()) {
                mapDim.getHighlightHandler().clearCachedHashes();
            }
        }
    }

    public static boolean getOptionValue(String name) {
        return getEffectiveConfigValue(XAERO_OPTIONS.get(name)) == Boolean.TRUE;
    }

    public static void trySettingCurrentProfileOption(@Nullable ConfigOption<Boolean> option, @Nullable Boolean value) {
        if (option == null) return;

        ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
        // don't update the config option if the server enforces a value
        if (configManager.getServerSynced().getEffective(option) != null) return;

        ConfigProfile currentProfile = configManager.getCurrentProfile();
        currentProfile.set(option, value);
        WorldMap.INSTANCE.getConfigs().getClientConfigProfileIO().save(currentProfile);
    }

    public static @Nullable Boolean getEffectiveConfigValue(@Nullable ConfigOption<Boolean> option) {
        if (option == null) return null;

        ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
        return configManager.getEffective(option);
    }
}
