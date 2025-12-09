package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.function.Supplier;

public class BlastProperty implements IMaterialProperty {

    public static final Codec<BlastProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("blast_temperature").forGetter(val -> val.blastTemperature),
            GasTier.CODEC.fieldOf("gas_tier").forGetter(val -> val.gasTier),
            Codec.INT.optionalFieldOf("duration_override", -1).forGetter(val -> val.durationOverride),
            Codec.INT.optionalFieldOf("eut_override", -1).forGetter(val -> val.EUtOverride),
            Codec.INT.optionalFieldOf("vacuum_duration_override", -1).forGetter(val -> val.vacuumDurationOverride),
            Codec.INT.optionalFieldOf("vacuum_eut_override", -1).forGetter(val -> val.vacuumEUtOverride))
            .apply(instance, BlastProperty::new));

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    @Getter
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     * <p>
     * Default: null, meaning no Gas EBF recipes.
     */
    @Getter
    @Setter
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    @Getter
    @Setter
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120.
     */
    @Getter
    @Setter
    private int EUtOverride = -1;

    /**
     * The duration of the Vacuum Freezer recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getMass() * 3
     */
    @Getter
    @Setter
    private int vacuumDurationOverride = -1;

    /**
     * The EU/t of the Vacuum Freezer recipe (if needed), overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120 EU/t.
     */
    @Getter
    @Setter
    private int vacuumEUtOverride = -1;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride,
                         int vacuumEUtOverride, int vacuumDurationOverride) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.EUtOverride = eutOverride;
        this.durationOverride = durationOverride;
        this.vacuumEUtOverride = vacuumEUtOverride;
        this.vacuumDurationOverride = vacuumDurationOverride;
    }

    /**
     * Default property constructor.
     */
    public BlastProperty() {
        this(0);
    }

    public void setBlastTemperature(int blastTemp) {
        if (blastTemp <= 0) throw new IllegalArgumentException("Blast Temperature must be greater than zero!");
        this.blastTemperature = blastTemp;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public enum GasTier implements StringRepresentable {

        // Tiers used by GTCEu
        LOW(() -> FluidIngredient.of(GTMaterials.Nitrogen.getFluidTag(), 1000)),
        MID(() -> FluidIngredient.of(GTMaterials.Helium.getFluidTag(), 100)),
        HIGH(() -> FluidIngredient.of(GTMaterials.Argon.getFluidTag(), 50)),

        // Tiers reserved for addons
        HIGHER(() -> FluidIngredient.of(GTMaterials.Neon.getFluidTag(), 25)),
        HIGHEST(() -> FluidIngredient.of(GTMaterials.Krypton.getFluidTag(), 10));

        public static final GasTier[] VALUES = values();
        public static final Codec<GasTier> CODEC = StringRepresentable.fromEnum(GasTier::values);

        @Override
        public String getSerializedName() {
            return name().toUpperCase(Locale.ROOT);
        }

        private Supplier<FluidIngredient> fluid;

        GasTier(Supplier<FluidIngredient> fluid) {
            this.fluid = GTMemoizer.memoize(fluid);
        }

        public void setFluid(Supplier<FluidIngredient> fluid) {
            this.fluid = GTMemoizer.memoize(fluid);
        }

        public FluidIngredient getFluid() {
            return fluid.get().copy();
        }
    }

    @SuppressWarnings("unused") // API, need to treat all of these as used
    public static class Builder {

        private int temp;
        private GasTier gasTier;
        private int eutOverride = -1;
        private int durationOverride = -1;
        private int vacuumEUtOverride = -1;
        private int vacuumDurationOverride = -1;

        public Builder() {}

        /**
         * Set the EBF temperature of this Material.
         * <br>
         * <br>
         * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
         * Ingot.<br>
         * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
         * EBF recipe.
         *
         * @param temperature The temperature of the recipe in the EBF.
         */
        public Builder temp(int temperature) {
            this.temp = temperature;
            return this;
        }

        /**
         * Set the EBF temperature and gas tier of this Material.
         * <br>
         * <br>
         * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
         * Ingot.<br>
         * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
         * EBF recipe.
         *
         * @param temperature The temperature of the recipe in the EBF.
         * @param gasTier     The {@link GasTier} of the Recipe. Will generate a second EBF recipe
         *                    using the specified gas of the tier for a speed bonus.
         */
        public Builder temp(int temperature, GasTier gasTier) {
            this.temp = temperature;
            this.gasTier = gasTier;
            return this;
        }

        /**
         * Set the EU/t of the EBF recipe for this Material.
         */
        public Builder blastStats(int eutOverride) {
            this.eutOverride = eutOverride;
            return this;
        }

        /**
         * Set the EU/t and duration of the EBF recipe for this Material.
         */
        public Builder blastStats(int eutOverride, int durationOverride) {
            this.eutOverride = eutOverride;
            this.durationOverride = durationOverride;
            return this;
        }

        /**
         * Set the EU/t of the Vacuum Freezer recipe for the Hot Ingot of this Material.
         */
        public Builder vacuumStats(int eutOverride) {
            this.vacuumEUtOverride = eutOverride;
            return this;
        }

        /**
         * Set the EU/t and duration of the Vacuum Freezer recipe for the Hot Ingot of this Material.
         */
        public Builder vacuumStats(int eutOverride, int durationOverride) {
            this.vacuumEUtOverride = eutOverride;
            this.vacuumDurationOverride = durationOverride;
            return this;
        }

        public BlastProperty build() {
            return new BlastProperty(temp, gasTier, eutOverride, durationOverride, vacuumEUtOverride,
                    vacuumDurationOverride);
        }
    }
}
