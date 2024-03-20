package com.cyvack.create_nerfed.config;

import com.cyvack.create_nerfed.backbone.ProcBlockInfoRegistry;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class CNGroupValues extends ConfigBase {

    private Map<ResourceLocation /*Block loc*/, ForgeConfigSpec.ConfigValue<Integer>> goldenGroupAmount = new HashMap<>();

    public final ConfigFloat groupingBonus = f(0.25f, 0f, "grouping_bonus", "The percent speed bonus processing machine grouping will give");

    @Override
    //Register all the group values within our pseudo registry, and their backing block resource locations
    public void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);

        builder.comment(".", "Configure how many of the same processing block should be in the same network to get bonuses")
                .push("groupings");//Create the grouping for our grouples
        ProcBlockInfoRegistry.DEFAULT_INFORMATION.forEach((rl, procInfo) -> goldenGroupAmount.put(rl, builder.comment("Modify block's grouping amount").define(rl.getPath(), procInfo.groupAmount())));
        builder.pop(); //Finish creating our grouples
    }

    public int getGoldenGroupAmount(Block block) {
        return goldenGroupAmount.get(RegisteredObjects.getKeyOrThrow(block)).get();
    }

    @Override
    public String getName() {
        return "grouping_values";
    }
}
