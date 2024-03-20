package com.cyvack.create_nerfed.backbone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class GroupingHandler {
    private final Map<Level, Set<BlockGrouping>> GROUPINGS = new HashMap<>();

    private final Set<GroupPosition> positionsToMerge = new HashSet<>();
    private final BlockGrouping DUMMY_LARGEST = new BlockGrouping(null, BlockGrouping.DUMMY_POS_POS);

    public BlockGrouping createGroup(GroupPosition groupPos, Level level) {
        if (!level.isClientSide) {
            check(level);
            BlockGrouping bg = new BlockGrouping(level, groupPos);
            bg.totalRebuild();
            GROUPINGS.get(level).add(bg);
            return bg;
        } else {
            return null;
        }
    }

    public void unloadLevel(Level level) {
        GROUPINGS.remove(level);
    }


    public BlockGrouping getGroupFromPos(Level level, Block type,  BlockPos groupPos) {
        return getGroupFromPos(level, type, null, groupPos);
    }

    public BlockGrouping getGroupFromPos(Level level, Block type, BlockGrouping excluded, BlockPos groupPos) {
        check(level);
        for (BlockGrouping group : GROUPINGS.get(level)) {
            if (group == excluded || group.invalid)
                continue;

            if (!group.compareTypes(type))
                continue;

            if (group.contains(groupPos))
                return group;
        }

        return null;
    }

    public List<BlockGrouping> getSurroundingGroupsExcluding(Level level, BlockGrouping excluded, GroupPosition groupPos) {
        check(level);

        List<BlockGrouping> groups = new ArrayList<>();
        BlockPos.MutableBlockPos mut = groupPos.pos.mutable();
        for (BlockGrouping group : GROUPINGS.get(level)) {
            if (group.equals(excluded) || group.invalid)
                continue;

            if (!group.compareTypes(groupPos.posInfo.getBlockType()))
                continue;

            for (Direction dir : Direction.values()) {
                mut.setWithOffset(groupPos.pos, dir);
                if (group.contains(mut) && !groups.contains(group)) {
                    groups.add(group);
                }
            }
        }

        return groups;
    }

    public List<BlockGrouping> getSurroundingGroupsFrom(Level level, GroupPosition groupPos) {
        check(level);
        BlockPos.MutableBlockPos mut = groupPos.pos.mutable();

        List<BlockGrouping> groups = new ArrayList<>();
        for (BlockGrouping group : GROUPINGS.get(level)) {
            if (!group.compareTypes(groupPos.posInfo.getBlockType()) || group.invalid)
                continue;

            for (Direction dir : Direction.values()) {
                mut.setWithOffset(groupPos.pos, dir);
                if (group.contains(mut) && !groups.contains(group)) {
                    groups.add(group);
                }
            }
        }

        return groups;
    }

    public void mergeAIntoB(BlockGrouping groupA, BlockGrouping groupB) {
        //Unsure if needed


    }

    public void mergeGroups(List<BlockGrouping> groups, Level level, boolean forceTotalRebuild, GroupPosition... additional) {
        BlockGrouping largest = DUMMY_LARGEST;
        for (BlockGrouping group : groups) {
            if (largest.getSize() < group.getSize())
                largest = group;
        }

        if (forceTotalRebuild) {

        } else {
            //Just in case
            if (groups.size() < 2)
                return;

            positionsToMerge.addAll(List.of(additional));

            //remove the largest so we don't iterate over it again
            groups.remove(largest);

            for (BlockGrouping group : groups) {
                positionsToMerge.addAll(group.getGroupPositions());
                group.clearAndMarkInvalid();
                removeBlockGroup(group, level);
            }

            //rebuild the heatmap of the largest group
            largest.forceInsertPositions(positionsToMerge);
            largest.rebuild();
        }

        positionsToMerge.clear();
    }

    public void removeBlockGroup(BlockGrouping group, Level level) {
        check(level);
        GROUPINGS.get(level).remove(group);
    }

    private void check(Level level) {
        GROUPINGS.computeIfAbsent(level, ($) -> new HashSet<>());
    }
}
