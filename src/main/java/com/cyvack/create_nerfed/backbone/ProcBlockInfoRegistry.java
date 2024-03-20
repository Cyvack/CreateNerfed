package com.cyvack.create_nerfed.backbone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

import static com.simibubi.create.AllBlocks.*;

/**
 * "registry"
 */
public class ProcBlockInfoRegistry {
    public static final Map<ResourceLocation /*block loc*/, ProcessingBlockInfo> DEFAULT_INFORMATION = new HashMap<>();

    public static final int DEFAULT_GROUP_AMOUNT = 10;

    public static void registerInfo(ResourceLocation blockLoc, ProcessingBlockInfo info) {
        if (DEFAULT_INFORMATION.containsKey(blockLoc)) {
            return; //TODO: create warning
        }

        DEFAULT_INFORMATION.put(blockLoc, info);
    }

    public static ProcessingBlockInfo createInfo(int groupAmount, float speedMult) {
        return new ProcessingBlockInfo(groupAmount, speedMult);
    }

    public static boolean contains(ResourceLocation blockLoc ){
        return DEFAULT_INFORMATION.containsKey(blockLoc);
    }

    public static void register(){
        registerInfo(MECHANICAL_PRESS.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));
        registerInfo(MECHANICAL_MIXER.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));
        registerInfo(MECHANICAL_SAW.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));
        registerInfo(MILLSTONE.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));
        registerInfo(SPOUT.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));

        registerInfo(CRUSHING_WHEEL.getId(), createInfo(DEFAULT_GROUP_AMOUNT, 1));
    }

    public record ProcessingBlockInfo(int groupAmount, float speedMult) {
    }
}
