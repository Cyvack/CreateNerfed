package com.cyvack.create_nerfed.backbone;

import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public interface GroupInformation {

    Block getBlockType();

    GroupPosition getGroupPos();

    Consumer<BlockGrouping> addCallback();

    Consumer<BlockGrouping> removeCallBack();

    boolean isValid();
}
