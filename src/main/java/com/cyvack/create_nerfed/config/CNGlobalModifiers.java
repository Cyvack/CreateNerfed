package com.cyvack.create_nerfed.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CNGlobalModifiers extends ConfigBase {

    protected static final float MIN = 0.25f;
    public static final String WARNING = "Higher the number, the longer it will take to process items, and vise versa";

    public final ConfigFloat globalProcessingDurationMult = f(1, MIN, getMultName("globalProcessingDuration"), "Multipliers used for global processing duration for machines like", "Applicable to: anything that uses the processing recipe type", WARNING);
    public final ConfigFloat globalProcessingSpeedMult = f(1, MIN, getMultName("globalProcessingSpeed"), "Multipliers used for global processing speed for machines", WARNING);

    protected static String getMultName(String blockName) {
        return blockName + "Multiplier";
    }

    @Override
    public String getName() {
        return "global_modifiers";
    }
}
