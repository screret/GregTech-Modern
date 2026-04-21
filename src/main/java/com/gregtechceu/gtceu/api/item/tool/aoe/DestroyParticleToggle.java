package com.gregtechceu.gtceu.api.item.tool.aoe;

/**
 * Utility class for enabling/disabling automatic block destruction particles.
 *
 * <p>
 * This should always be used in a try-with-resources block to automatically re-enable them after, like this:
 * <pre>{@code
 * try (var ignored = DestroyParticleToggle.enabled(false)) {
 *     // ...
 * }
 * // particles are enabled again here
 * }</pre>
 */
public final class DestroyParticleToggle implements AutoCloseable {

    private static final DestroyParticleToggle INSTANCE = new DestroyParticleToggle();

    private boolean enableDestroyParticles = true;

    public static DestroyParticleToggle runWithParticles(boolean enabled) {
        INSTANCE.enableDestroyParticles = enabled;
        return INSTANCE;
    }

    public static boolean destroyParticlesEnabled() {
        return INSTANCE.enableDestroyParticles;
    }

    @Override
    public void close() {
        enableDestroyParticles = true;
    }

    private DestroyParticleToggle() {}
}
