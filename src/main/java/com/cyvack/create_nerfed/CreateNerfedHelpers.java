package com.cyvack.create_nerfed;

import com.cyvack.create_nerfed.backbone.BlockGrouping;
import com.cyvack.create_nerfed.config.CNConfig;
import com.cyvack.create_nerfed.mixinterfaces.BlockGroupGetter;
import com.cyvack.create_nerfed.mixinterfaces.GroupAmountGetterBE;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import net.minecraft.world.level.block.Block;

public class CreateNerfedHelpers {

    //TODO: merge these into one method once grouping is implemented
    public static double getModifiedSpeed(SmartBlockEntity be, double value, boolean multiply) {
        double mult = getSpeedMult(be.getBlockState().getBlock());
        if (CNConfig.server().groupProcessingEnabled.get())
            mult /= getGroupEffeciency(be);

        return Math.max(1, multiply ? (value * mult) : (value / mult));
    }

    private static double getSpeedMult(Block block) {
        Double blockSpeed = CNConfig.server().processingSpeed.getSpeedMultipliers().get(RegisteredObjects.getKeyOrThrow(block)).get();
        return Math.max(0.25f, blockSpeed * CNConfig.server().processingSpeed.modifiers.globalProcessingSpeedMult.get());
    }

    /**
     * @return A value between 1 and the configured percent max
     */
    public static double getGroupEffeciency(SmartBlockEntity be) {
        BlockGrouping group = ((BlockGroupGetter) be).getGroup();
        int groupAmount = 0;
        if (group != null)
            groupAmount = group.getSize();

        int goldenGroupAmount = CNConfig.server().groupValues.getGoldenGroupAmount(be.getBlockState().getBlock());
        if (goldenGroupAmount == 0 || groupAmount == 0 || groupAmount < goldenGroupAmount / 2)
            return 1;

        //Ensure that once the critical mass point as been reached, don't decrease the bonus
        if (groupAmount > goldenGroupAmount)
            groupAmount = goldenGroupAmount;

        Double percentBonus = CNConfig.server().groupValues.groupingBonus.get();
        double numerator = Math.abs(groupAmount - goldenGroupAmount) * percentBonus;
        double denominator = goldenGroupAmount / 2d;
        return Math.max(1, 1 + (percentBonus - (numerator / denominator)));
    }
}
