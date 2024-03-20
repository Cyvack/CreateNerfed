package com.cyvack.create_nerfed.backbone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class GroupPosition {
    private static final DummyFunctions DUMMY_FUNCTIONS = new DummyFunctions();

    public GroupInformation posInfo;
    public final BlockPos pos;

    private int heatValue = -1;

    boolean virtual = false;

    public int checkedAmount = 0;

    public GroupPosition(int heatValue) {
        this.pos = new BlockPos(0, 0, 0);
        this.heatValue = heatValue;
        this.posInfo = DUMMY_FUNCTIONS;
    }

    public GroupPosition(final BlockPos pos, final GroupInformation callbackFunctions) {
        this.pos = pos.immutable();
        this.posInfo = callbackFunctions;
    }

    public int getHeatValue() {
        return heatValue;
    }

    public void setHeatValue(int heatValue) {
        this.heatValue = heatValue;
    }

    public void toggleVirtual() {
        virtual = !virtual;
    }

    public void clear() {
        setHeatValue(-1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupPosition))
            return false;

        return ((GroupPosition) obj).pos.equals(pos) && ((GroupPosition) obj).posInfo.getBlockType().equals(posInfo.getBlockType());
    }

    private static class DummyFunctions implements GroupInformation {

        @Override
        public Block getBlockType() {
            return Blocks.AIR;
        }

        @Override
        public GroupPosition getGroupPos() {
            return null;
        }

        @Override
        public Consumer<BlockGrouping> addCallback() {
            return (BlockGrouping) -> {
            };
        }

        @Override
        public Consumer<BlockGrouping> removeCallBack() {
            return (BlockGrouping) -> {
            };
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }
}
