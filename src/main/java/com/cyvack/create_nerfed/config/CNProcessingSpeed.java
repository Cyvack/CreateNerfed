package com.cyvack.create_nerfed.config;

import com.cyvack.create_nerfed.backbone.ProcBlockInfoRegistry;
import com.simibubi.create.foundation.config.ConfigBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class CNProcessingSpeed extends ConfigBase {

    protected static final float MIN = 0.25f;

    public static final String WARNING = "Higher the number, the longer it will take to process items, and vise versa";

    private final Map<ResourceLocation /*block loc*/, ForgeConfigSpec.ConfigValue<Double>> speedMultipliers = new HashMap<>();

    public final CNGlobalModifiers modifiers = nested(1, CNGlobalModifiers::new, "Global modifiers for most processing machines");

    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
        builder.comment(".", "Configure the processing speed of certain processing blocks", WARNING)
                .push("speed_multipliers");//Create the grouping for our grouples
        ProcBlockInfoRegistry.DEFAULT_INFORMATION.forEach((rl, procInfo) -> speedMultipliers.put(rl, builder.comment("Modify block's processing speed", WARNING).defineInRange(rl.getPath(), procInfo.speedMult(), MIN, Double.MAX_VALUE)));
        builder.pop(); //Finish creating our grouples

        super.registerAll(builder);
    }

    public Map<ResourceLocation, ForgeConfigSpec.ConfigValue<Double>> getSpeedMultipliers() {
        return speedMultipliers;
    }

    @Override
    public String getName() {
        return "processing_speed";
    }
}
