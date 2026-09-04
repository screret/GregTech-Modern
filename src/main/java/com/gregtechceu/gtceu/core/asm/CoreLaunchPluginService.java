package com.gregtechceu.gtceu.core.asm;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Mass ASM transforming system based on the one from <a
 * href=
 * "https://github.com/embeddedt/ModernFix/blob/a6a3497b8169471625d15c5da90a4c5b9b1940f1/src/main/java/org/embeddedt/modernfix/core/launchplugin/CoreLaunchPluginService.java">ModernFix</a>
 */
public class CoreLaunchPluginService implements ILaunchPluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger("GTCEuLaunchPlugin");

    public static void install() {
        try {
            Field launchPluginsField = Launcher.class.getDeclaredField("launchPlugins");
            launchPluginsField.setAccessible(true);
            LaunchPluginHandler launchPluginHandler = (LaunchPluginHandler) launchPluginsField.get(Launcher.INSTANCE);
            Field pluginsField = LaunchPluginHandler.class.getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> plugins = (Map<String, ILaunchPluginService>) pluginsField
                    .get(launchPluginHandler);

            var service = new CoreLaunchPluginService();
            var newMap = new LinkedHashMap<>(plugins);
            newMap.put(service.name(), service);
            pluginsField.set(launchPluginHandler, newMap);
        } catch (Exception e) {
            LOGGER.error("Error installing launch plugin service", e);
        }
    }

    @Override
    public String name() {
        return "gtceu";
    }

    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.AFTER);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);

    private static final SequencedMap<String, Transformer> CLASS_TRANSFORMERS = new LinkedHashMap<>(Map.of(
    // "net.minecraftforge.common.capabilities.CapabilityProvider", new CapabilityProviderTransformer()
    ));

    private static final List<Transformer> GLOBAL_TRANSFORMERS = List.of(

    );

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return isEmpty ? NAY : YAY;
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (classNode == null) {
            return 0;
        }
        int flags = 0;

        Transformer transformer = CLASS_TRANSFORMERS.get(classType.getClassName());
        if (transformer != null) {
            flags = transformer.transform(classNode);
        }

        if (!GLOBAL_TRANSFORMERS.isEmpty()) {
            for (Transformer globalTransformer : GLOBAL_TRANSFORMERS) {
                flags |= globalTransformer.transform(classNode);
            }
        }

        return flags;
    }

    public interface Transformer {

        @MagicConstant(flagsFromClass = ILaunchPluginService.ComputeFlags.class)
        int transform(ClassNode node);
    }
}
